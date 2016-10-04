package org.molgenis.data.semanticsearch.service.impl;

import static com.google.common.collect.Sets.newLinkedHashSet;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.molgenis.data.QueryRule.Operator.DIS_MAX;
import static org.molgenis.data.QueryRule.Operator.FUZZY_MATCH;
import static org.molgenis.data.semanticsearch.utils.SemanticSearchServiceUtils.getLowerCaseTerms;
import static org.molgenis.data.semanticsearch.utils.SemanticSearchServiceUtils.splitRemoveStopWords;
import static org.molgenis.ontology.core.repository.OntologyTermRepository.DEFAULT_EXPANSION_LEVEL;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.elasticsearch.common.base.Joiner;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.meta.model.AttributeMetaDataMetaData;
import org.molgenis.data.semanticsearch.service.QueryExpansionService;
import org.molgenis.data.semanticsearch.service.bean.SearchParam;
import org.molgenis.data.semanticsearch.service.bean.TagGroup;
import org.molgenis.data.semanticsearch.utils.SemanticSearchServiceUtils;
import org.molgenis.ontology.core.model.OntologyTermImpl;
import org.molgenis.ontology.core.service.OntologyService;
import org.molgenis.ontology.ic.TermFrequencyService;
import org.molgenis.ontology.utils.Stemmer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

public class QueryExpansionServiceImpl implements QueryExpansionService
{
	private static final Logger LOG = LoggerFactory.getLogger(QueryExpansionServiceImpl.class);

	private static final float LEXICAL_QUERY_BOOSTVALUE = 1.0f;

	private static DecimalFormat DECIMAL_FORMATTER = new DecimalFormat("#.#####", new DecimalFormatSymbols(Locale.US));

	private final TermFrequencyService termFrequencyService;
	private final OntologyService ontologyService;

	private final static String CARET_CHARACTER = "^";
	private final static String ESCAPED_CARET_CHARACTER = "\\^";
	private Joiner termJoiner = Joiner.on(' ');

	private LoadingCache<OntologyTermImpl, List<String>> cachedOntologyTermQuery = CacheBuilder.newBuilder()
			.maximumSize(1000).expireAfterWrite(1, TimeUnit.HOURS).build(new CacheLoader<OntologyTermImpl, List<String>>()
			{
				public List<String> load(OntologyTermImpl ontologyTerm)
				{
					return getExpandedQueriesFromOntologyTerm(ontologyTerm);
				}
			});

	@Autowired
	public QueryExpansionServiceImpl(OntologyService ontologyService, TermFrequencyService termFrequencyService)
	{
		this.ontologyService = requireNonNull(ontologyService);
		this.termFrequencyService = requireNonNull(termFrequencyService);
	}

	/**
	 * Create a disMaxJunc query rule based on the given search terms as well as the tag groups
	 *
	 * @param tagGroups
	 * @param lexicalQueries
	 *
	 * @return disMaxJunc queryRule
	 */
	public QueryRule expand(SearchParam searchParam)
	{
		List<QueryRule> rules = new ArrayList<>();

		Set<String> lexicalQueries = searchParam.getLexicalQueries();

		List<TagGroup> tagGroups = searchParam.getTagGroups();

		// Parse the lexical queries
		if (lexicalQueries != null && !lexicalQueries.isEmpty())
		{
			List<String> queryTerms = lexicalQueries.stream().filter(StringUtils::isNotBlank)
					.map(this::parseQueryString).map(this::boostLexicalQuery).collect(toList());

			QueryRule createDisMaxQueryRuleForTerms = createDisMaxQueryRuleForTerms(queryTerms,
					LEXICAL_QUERY_BOOSTVALUE);

			if (createDisMaxQueryRuleForTerms != null)
			{
				rules.add(createDisMaxQueryRuleForTerms);
			}
		}

		// Collection the queries from ontology terms and parse them
		Multimap<String, TagGroup> groupWithSameSynonym = LinkedHashMultimap.create();
		tagGroups.forEach(hit -> groupWithSameSynonym.put(hit.getMatchedWords(), hit));
		for (String synonym : groupWithSameSynonym.keySet())
		{
			List<TagGroup> ontologyTermGroup = Lists.newArrayList(groupWithSameSynonym.get(synonym));

			QueryRule queryRuleForOntologyTerms = createQueryRuleForOntologyTerms(ontologyTermGroup);

			if (queryRuleForOntologyTerms != null)
			{
				rules.add(queryRuleForOntologyTerms);
			}
		}

		QueryRule disMaxQueryRule = null;
		if (rules.size() > 0)
		{
			disMaxQueryRule = new QueryRule(rules);
			disMaxQueryRule.setOperator(DIS_MAX);
		}

		return disMaxQueryRule;
	}

	/**
	 * Creates a disMax query rule that only contains all information from the ontology terms as well as their children
	 * 
	 * @param ontologyTermHits
	 * @return
	 */
	QueryRule createQueryRuleForOntologyTerms(List<TagGroup> ontologyTermHits)
	{
		QueryRule queryRule = null;

		if (ontologyTermHits.size() > 0)
		{
			float score = ontologyTermHits.get(0).getScore();

			// Put ontologyTerms with the same synonym in a map
			Multimap<OntologyTermImpl, OntologyTermImpl> atomicOntologyTermGroups = groupAtomicOntologyTermsBySynonym(
					ontologyTermHits);

			Set<OntologyTermImpl> ontologyTermGroupKeys = atomicOntologyTermGroups.keySet();

			if (ontologyTermGroupKeys.size() > 1)
			{
				Map<OntologyTermImpl, Float> ontologyTermGroupWeight = normalizeBoostValueForOntologyTermGroup(
						atomicOntologyTermGroups);

				Function<OntologyTermImpl, QueryRule> ontologyTermGroupToQueryRule = groupKey -> {

					List<String> queryTermsFromSameGroup = atomicOntologyTermGroups.get(groupKey).stream()
							.flatMap(ot -> getCachedQueriesForOntologyTerm(ot).stream()).collect(toList());

					return createDisMaxQueryRuleForTerms(queryTermsFromSameGroup,
							ontologyTermGroupWeight.get(groupKey));
				};

				queryRule = createShouldQueryRule(
						ontologyTermGroupKeys.stream().map(ontologyTermGroupToQueryRule).collect(toList()), score);
			}
			else
			{
				OntologyTermImpl firstOntologyTermGroupKey = Iterables.get(ontologyTermGroupKeys, 0);

				List<String> queryTerms = atomicOntologyTermGroups.get(firstOntologyTermGroupKey).stream()
						.flatMap(ot -> getCachedQueriesForOntologyTerm(ot).stream()).collect(toList());

				queryRule = createDisMaxQueryRuleForTerms(queryTerms, score);
			}
		}

		return queryRule;
	}

	/**
	 * Gets the cached list of queries from the {@link LoadingCache}
	 * 
	 * @param ontologyTerm
	 * @param queryExpansionParam
	 * @return a list of cached queries
	 */
	List<String> getCachedQueriesForOntologyTerm(OntologyTermImpl ontologyTerm)
	{
		try
		{
			return cachedOntologyTermQuery.get(ontologyTerm);
		}
		catch (ExecutionException e)
		{
			LOG.error(e.getMessage());
		}

		return emptyList();
	}

	/**
	 * Create a list of string queries based on the information collected from current ontologyterm including label,
	 * synonyms and child ontologyterms
	 * 
	 * @param ontologyTerm
	 * @return
	 */
	List<String> getExpandedQueriesFromOntologyTerm(OntologyTermImpl ontologyTerm)
	{
		List<String> queryTerms = getLowerCaseTerms(ontologyTerm).stream().map(this::parseQueryString)
				.collect(toList());

		Function<OntologyTermImpl, Stream<String>> mapChildOntologyTermToQueries = relatedOntologyTerm -> getLowerCaseTerms(
				relatedOntologyTerm).stream().map(query -> parseBoostQueryString(query,
						Math.pow(0.5, ontologyService.getOntologyTermDistance(ontologyTerm, relatedOntologyTerm))));

		LOG.trace("Started retrieving the children for the OntologyTerm: {}", ontologyTerm.toString());

		List<String> queryTermsFromChildOntologyTerms = StreamSupport
				.stream(ontologyService.getChildren(ontologyTerm, DEFAULT_EXPANSION_LEVEL).spliterator(), false)
				.flatMap(mapChildOntologyTermToQueries).collect(toList());

		LOG.trace("Retrieved {}", queryTermsFromChildOntologyTerms.size());

		queryTerms.addAll(queryTermsFromChildOntologyTerms);

		return queryTerms;
	}

	private Map<OntologyTermImpl, Float> normalizeBoostValueForOntologyTermGroup(
			Multimap<OntologyTermImpl, OntologyTermImpl> ontologyTermGroups)
	{
		Function<OntologyTermImpl, Double> groupKeyToGroupWeight = key -> ontologyTermGroups.get(key).stream()
				.map(SemanticSearchServiceUtils::getLowerCaseTerms).map(this::getBestInverseDocumentFrequency)
				.mapToDouble(tf -> (double) tf).max().orElse(1.0d);

		Map<OntologyTermImpl, Double> ontologyTermGroupWeight = ontologyTermGroups.keySet().stream()
				.collect(Collectors.toMap(key -> key, groupKeyToGroupWeight));

		double maxIdfValue = ontologyTermGroupWeight.values().stream().mapToDouble(Double::doubleValue).max()
				.orElse(1.0d);

		return ontologyTermGroupWeight.entrySet().stream()
				.collect(toMap(Entry::getKey, e -> new Float(e.getValue() / maxIdfValue)));
	}

	private Multimap<OntologyTermImpl, OntologyTermImpl> groupAtomicOntologyTermsBySynonym(List<TagGroup> ontologyTermHits)
	{
		Multimap<OntologyTermImpl, OntologyTermImpl> multiMap = LinkedHashMultimap.create();
		ontologyTermHits.get(0).getOntologyTerms().forEach(ot -> multiMap.put(ot, ot));

		ontologyTermHits.stream().skip(1).flatMap(hit -> hit.getOntologyTerms().stream())
				.filter(ot -> !multiMap.containsKey(ot)).forEach(atomicOntologyTerm -> {

					OntologyTermImpl ontologyTermInTheMap = multiMap.keySet().stream()
							.filter(ot -> hasSameSynonyms(ot, atomicOntologyTerm)).findFirst().orElse(null);

					if (ontologyTermInTheMap != null)
					{
						multiMap.put(ontologyTermInTheMap, atomicOntologyTerm);
					}
				});

		return multiMap;
	}

	private boolean hasSameSynonyms(OntologyTermImpl ontologyTerm1, OntologyTermImpl ontologyTerm2)
	{
		List<String> stemmedSynonymsOfOt1 = getLowerCaseTerms(ontologyTerm1).stream().map(Stemmer::cleanStemPhrase)
				.collect(toList());

		return getLowerCaseTerms(ontologyTerm2).stream()
				.anyMatch(synonym -> stemmedSynonymsOfOt1.contains(Stemmer.cleanStemPhrase(synonym)));
	}

	/**
	 * Create disMaxJunc query rule based a list of queryTerm. All queryTerms are lower cased and stop words are removed
	 * 
	 * @param queryTerms
	 * @return disMaxJunc queryRule
	 */
	QueryRule createDisMaxQueryRuleForTerms(List<String> queryTerms, Float boostValue)
	{
		List<QueryRule> rules = new ArrayList<QueryRule>();
		newLinkedHashSet(queryTerms).stream().filter(StringUtils::isNotEmpty)
				.map(string -> QueryParser.escape(string).replace(ESCAPED_CARET_CHARACTER, CARET_CHARACTER))
				.forEach(query -> {
					rules.add(new QueryRule(AttributeMetaDataMetaData.LABEL, FUZZY_MATCH, query));
					rules.add(new QueryRule(AttributeMetaDataMetaData.DESCRIPTION, FUZZY_MATCH, query));
				});

		QueryRule finalDisMaxQuery = null;
		if (rules.size() > 0)
		{
			finalDisMaxQuery = new QueryRule(rules);
			finalDisMaxQuery.setOperator(Operator.DIS_MAX);
		}

		if (finalDisMaxQuery != null && boostValue != null && boostValue.intValue() != 0)
		{
			finalDisMaxQuery.setValue(boostValue);
		}

		return finalDisMaxQuery;
	}

	private QueryRule createShouldQueryRule(List<QueryRule> queryRules, Float boostValue)
	{
		QueryRule shouldQueryRule = null;
		if (queryRules.size() > 0)
		{
			shouldQueryRule = new QueryRule(new ArrayList<QueryRule>());
			shouldQueryRule.setOperator(Operator.SHOULD);
			shouldQueryRule.getNestedRules().addAll(queryRules);
		}

		if (shouldQueryRule != null && boostValue != null && boostValue > 0)
		{
			shouldQueryRule.setValue(boostValue);
		}

		return shouldQueryRule;
	}

	private String boostLexicalQuery(String lexicalQuery)
	{
		Map<String, Float> collect = SemanticSearchServiceUtils.splitIntoUniqueTerms(lexicalQuery).stream()
				.collect(Collectors.toMap(t -> t, t -> termFrequencyService.getTermFrequency(t)));

		double max = collect.values().stream().mapToDouble(value -> (double) value).max().orElse(1.0d);

		Map<String, Float> weightedBoostValue = collect.entrySet().stream()
				.collect(Collectors.toMap(Entry::getKey, entry -> new Float(entry.getValue() / max)));

		List<String> boostedWords = SemanticSearchServiceUtils.splitIntoUniqueTerms(lexicalQuery).stream()
				.map(term -> term + CARET_CHARACTER + weightedBoostValue.get(term)).collect(Collectors.toList());

		return termJoiner.join(boostedWords);
	}

	private Float getBestInverseDocumentFrequency(Set<String> terms)
	{
		Optional<String> findFirst = terms.stream().sorted(new Comparator<String>()
		{
			public int compare(String o1, String o2)
			{
				return Integer.compare(o1.length(), o2.length());
			}
		}).findFirst();

		return findFirst.isPresent() ? termFrequencyService.getTermFrequency(findFirst.get()) : null;
	}

	String parseQueryString(String queryString)
	{
		return termJoiner.join(splitRemoveStopWords(queryString));
	}

	String parseBoostQueryString(String queryString, double boost)
	{
		return termJoiner.join(splitRemoveStopWords(queryString).stream()
				.map(w -> w + CARET_CHARACTER + DECIMAL_FORMATTER.format(boost)).collect(toList()));
	}
}