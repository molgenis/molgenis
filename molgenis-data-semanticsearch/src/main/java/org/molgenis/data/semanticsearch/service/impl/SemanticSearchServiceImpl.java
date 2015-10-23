package org.molgenis.data.semanticsearch.service.impl;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.spell.StringDistance;
import org.elasticsearch.common.base.Joiner;
import org.elasticsearch.common.collect.Lists;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.meta.AttributeMetaDataMetaData;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.semanticsearch.explain.bean.ExplainedAttributeMetaData;
import org.molgenis.data.semanticsearch.explain.bean.ExplainedQueryString;
import org.molgenis.data.semanticsearch.explain.service.ElasticSearchExplainService;
import org.molgenis.data.semanticsearch.semantic.Hit;
import org.molgenis.data.semanticsearch.service.SemanticSearchService;
import org.molgenis.data.semanticsearch.string.NGramDistanceAlgorithm;
import org.molgenis.data.semanticsearch.string.Stemmer;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.core.model.Ontology;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.ontology.core.service.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Splitter;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Ordering;

import autovalue.shaded.com.google.common.common.collect.Sets;

public class SemanticSearchServiceImpl implements SemanticSearchService
{
	private static final Logger LOG = LoggerFactory.getLogger(SemanticSearchServiceImpl.class);

	private final DataService dataService;
	private final OntologyService ontologyService;
	private final MetaDataService metaDataService;
	private final SemanticSearchServiceHelper semanticSearchServiceHelper;
	private final ElasticSearchExplainService elasticSearchExplainService;

	public static final int MAX_NUM_TAGS = 100;
	private static final float CUTOFF = 0.4f;
	private Splitter termSplitter = Splitter.onPattern("[^\\p{IsAlphabetic}]+");
	private Joiner termJoiner = Joiner.on(' ');
	private static final String UNIT_ONTOLOGY_IRI = "http://purl.obolibrary.org/obo/uo.owl";

	// We only explain the top 10 suggested attributes because beyond that the attributes are not high quliaty anymore
	private static final int MAX_NUMBER_EXPLAINED_ATTRIBUTES = 10;

	@Autowired
	public SemanticSearchServiceImpl(DataService dataService, OntologyService ontologyService,
			MetaDataService metaDataService, SemanticSearchServiceHelper semanticSearchServiceHelper,
			ElasticSearchExplainService elasticSearchExplainService)
	{
		this.dataService = requireNonNull(dataService);
		this.ontologyService = requireNonNull(ontologyService);
		this.metaDataService = requireNonNull(metaDataService);
		this.semanticSearchServiceHelper = requireNonNull(semanticSearchServiceHelper);
		this.elasticSearchExplainService = requireNonNull(elasticSearchExplainService);
	}

	@Override
	public Map<AttributeMetaData, ExplainedAttributeMetaData> findAttributes(EntityMetaData sourceEntityMetaData,
			Set<String> queryTerms, Collection<OntologyTerm> ontologyTerms)
	{
		Iterable<String> attributeIdentifiers = semanticSearchServiceHelper
				.getAttributeIdentifiers(sourceEntityMetaData);

		QueryRule disMaxQueryRule = semanticSearchServiceHelper.createDisMaxQueryRuleForAttribute(queryTerms,
				ontologyTerms);

		List<QueryRule> finalQueryRules = Lists
				.newArrayList(new QueryRule(AttributeMetaDataMetaData.IDENTIFIER, Operator.IN, attributeIdentifiers));

		if (disMaxQueryRule.getNestedRules().size() > 0)
		{
			finalQueryRules.addAll(Arrays.asList(new QueryRule(Operator.AND), disMaxQueryRule));
		}

		Iterable<Entity> attributeMetaDataEntities = dataService.findAll(AttributeMetaDataMetaData.ENTITY_NAME,
				new QueryImpl(finalQueryRules));

		Map<String, String> collectExpanedQueryMap = semanticSearchServiceHelper.collectExpandedQueryMap(queryTerms,
				ontologyTerms);

		// Because the explain-API can be computationally expensive we limit the explanation to the top 10 attributes
		Map<AttributeMetaData, ExplainedAttributeMetaData> explainedAttributes = new LinkedHashMap<>();
		int count = 0;
		for (Entity attributeEntity : attributeMetaDataEntities)
		{
			AttributeMetaData attribute = sourceEntityMetaData
					.getAttribute(attributeEntity.getString(AttributeMetaDataMetaData.NAME));
			if (count < MAX_NUMBER_EXPLAINED_ATTRIBUTES)
			{
				Set<ExplainedQueryString> explanations = convertAttributeEntityToExplainedAttribute(attributeEntity,
						sourceEntityMetaData, collectExpanedQueryMap, finalQueryRules);

				boolean singleMatchHighQuality = isSingleMatchHighQuality(queryTerms, collectExpanedQueryMap.values(),
						explanations);

				explainedAttributes.put(attribute,
						ExplainedAttributeMetaData.create(attribute, explanations, singleMatchHighQuality));
			}
			else
			{
				explainedAttributes.put(attribute, ExplainedAttributeMetaData.create(attribute));
			}
			count++;
		}

		return explainedAttributes;
	}

	boolean isSingleMatchHighQuality(Collection<String> queryTerms, Collection<String> ontologyTermQueries,
			Iterable<ExplainedQueryString> explanations)
	{
		Map<String, Double> matchedTags = new HashMap<>();

		for (ExplainedQueryString explanation : explanations)
		{
			matchedTags.put(explanation.getTagName().toLowerCase(), explanation.getScore());
		}

		ontologyTermQueries.removeAll(queryTerms);

		if (queryTerms.size() > 0 && queryTerms.stream().anyMatch(token -> isGoodMatch(matchedTags, token)))
			return true;

		if (ontologyTermQueries.size() > 0
				&& ontologyTermQueries.stream().allMatch(token -> isGoodMatch(matchedTags, token)))
			return true;

		return false;
	}

	boolean isGoodMatch(Map<String, Double> matchedTags, String label)
	{
		label = label.toLowerCase();
		return matchedTags.containsKey(label) && matchedTags.get(label).intValue() == 100
				|| Sets.newHashSet(label.split(" ")).stream()
						.allMatch(word -> matchedTags.containsKey(word) && matchedTags.get(word).intValue() == 100);
	}

	@Override
	public Map<AttributeMetaData, ExplainedAttributeMetaData> decisionTreeToFindRelevantAttributes(
			EntityMetaData sourceEntityMetaData, AttributeMetaData targetAttribute,
			Collection<OntologyTerm> ontologyTermsFromTags, Set<String> searchTerms)
	{
		Set<String> queryTerms = createLexicalSearchQueryTerms(targetAttribute, searchTerms);

		Collection<OntologyTerm> ontologyTerms = ontologyTermsFromTags;

		if (null != searchTerms && !searchTerms.isEmpty())
		{
			Set<String> escapedSearchTerms = searchTerms.stream().filter(StringUtils::isNotBlank)
					.map(QueryParser::escape).collect(Collectors.toSet());
			ontologyTerms = ontologyService.findExcatOntologyTerms(ontologyService.getAllOntologiesIds(),
					escapedSearchTerms, MAX_NUM_TAGS);
		}
		else if (null == ontologyTerms || ontologyTerms.size() == 0)
		{
			List<String> allOntologiesIds = ontologyService.getAllOntologiesIds();
			Ontology unitOntology = ontologyService.getOntology(UNIT_ONTOLOGY_IRI);
			if (unitOntology != null)
			{
				allOntologiesIds.remove(unitOntology.getId());
			}
			Hit<OntologyTerm> ontologyTermHit = findTags(targetAttribute, allOntologiesIds);
			ontologyTerms = ontologyTermHit != null ? Arrays.asList(ontologyTermHit.getResult())
					: Collections.emptyList();
		}

		return findAttributes(sourceEntityMetaData, queryTerms, ontologyTerms);
	}

	/**
	 * A helper function to create a list of queryTerms based on the information from the targetAttribute as well as
	 * user defined searchTerms. If the user defined searchTerms exist, the targetAttribute information will not be
	 * used.
	 * 
	 * @param targetAttribute
	 * @param searchTerms
	 * @return list of queryTerms
	 */
	public Set<String> createLexicalSearchQueryTerms(AttributeMetaData targetAttribute, Set<String> searchTerms)
	{
		Set<String> queryTerms = new HashSet<>();

		if (searchTerms != null && !searchTerms.isEmpty())
		{
			queryTerms.addAll(searchTerms);
		}

		if (queryTerms.size() == 0)
		{
			if (StringUtils.isNotBlank(targetAttribute.getLabel()))
			{
				queryTerms.add(targetAttribute.getLabel());
			}

			if (StringUtils.isNotBlank(targetAttribute.getDescription()))
			{
				queryTerms.add(targetAttribute.getDescription());
			}
		}

		return queryTerms;
	}

	/**
	 * A helper function to explain each of the matched attributes returned by the explain-API
	 * 
	 * @param attributeEntity
	 * @param sourceEntityMetaData
	 * @param collectExpanedQueryMap
	 * @param finalQueryRules
	 * @return
	 */
	public Set<ExplainedQueryString> convertAttributeEntityToExplainedAttribute(Entity attributeEntity,
			EntityMetaData sourceEntityMetaData, Map<String, String> collectExpanedQueryMap,
			List<QueryRule> finalQueryRules)
	{
		String attributeId = attributeEntity.getString(AttributeMetaDataMetaData.IDENTIFIER);
		String attributeName = attributeEntity.getString(AttributeMetaDataMetaData.NAME);
		AttributeMetaData attribute = sourceEntityMetaData.getAttribute(attributeName);
		if (attribute == null)
		{
			throw new MolgenisDataAccessException("The attributeMetaData : " + attributeName
					+ " does not exsit in EntityMetaData : " + sourceEntityMetaData.getName());
		}
		Explanation explanation = elasticSearchExplainService.explain(new QueryImpl(finalQueryRules),
				dataService.getEntityMetaData(AttributeMetaDataMetaData.ENTITY_NAME), attributeId);

		Set<ExplainedQueryString> detectedQueryStrings = elasticSearchExplainService
				.findQueriesFromExplanation(collectExpanedQueryMap, explanation);

		return detectedQueryStrings;
	}

	@Override
	public Map<AttributeMetaData, Hit<OntologyTerm>> findTags(String entity, List<String> ontologyIds)
	{
		Map<AttributeMetaData, Hit<OntologyTerm>> result = new LinkedHashMap<AttributeMetaData, Hit<OntologyTerm>>();
		EntityMetaData emd = metaDataService.getEntityMetaData(entity);
		for (AttributeMetaData amd : emd.getAtomicAttributes())
		{
			Hit<OntologyTerm> tag = findTags(amd, ontologyIds);
			if (tag != null)
			{
				result.put(amd, tag);
			}
		}
		return result;
	}

	@Override
	public Hit<OntologyTerm> findTags(AttributeMetaData attribute, List<String> ontologyIds)
	{
		String description = attribute.getDescription() == null ? attribute.getLabel() : attribute.getDescription();
		Set<String> searchTerms = splitIntoTerms(description);
		Stemmer stemmer = new Stemmer();

		if (LOG.isDebugEnabled())
		{
			LOG.debug("findOntologyTerms({},{},{})", ontologyIds, searchTerms, MAX_NUM_TAGS);
		}

		List<OntologyTerm> candidates = ontologyService.findOntologyTerms(ontologyIds, searchTerms, MAX_NUM_TAGS);

		if (LOG.isDebugEnabled())
		{
			LOG.debug("Candidates: {}", candidates);
		}

		List<Hit<OntologyTerm>> hits = candidates.stream()
				.filter(ontologyTerm -> filterOntologyTerm(splitIntoTerms(stemmer.stemAndJoin(searchTerms)),
						ontologyTerm, stemmer))
				.map(ontolgoyTerm -> Hit.<OntologyTerm> create(ontolgoyTerm,
						bestMatchingSynonym(ontolgoyTerm, searchTerms).getScore()))
				.sorted(Ordering.natural().reverse()).collect(Collectors.toList());

		if (LOG.isDebugEnabled())
		{
			LOG.debug("Hits: {}", hits);
		}

		Hit<OntologyTerm> result = null;
		String bestMatchingSynonym = null;
		for (Hit<OntologyTerm> hit : hits)
		{
			String bestMatchingSynonymForHit = bestMatchingSynonym(hit.getResult(), searchTerms).getResult();
			if (result == null)
			{
				result = hit;
				bestMatchingSynonym = bestMatchingSynonymForHit;
			}
			else
			{
				Set<String> jointTerms = Sets.union(splitIntoTerms(bestMatchingSynonym),
						splitIntoTerms(bestMatchingSynonymForHit));
				String joinedSynonyms = termJoiner.join(jointTerms);
				Hit<OntologyTerm> joinedHit = Hit.create(OntologyTerm.and(result.getResult(), hit.getResult()),
						distanceFrom(joinedSynonyms, searchTerms, stemmer));
				if (joinedHit.compareTo(result) > 0)
				{
					result = joinedHit;
				}
			}

			if (LOG.isDebugEnabled())
			{
				LOG.debug("result: {}", result);
			}
		}
		if (result != null && result.getScore() >= CUTOFF)
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Tag {} with {}", attribute, result);
			}
			return result;
		}
		return null;
	}

	private boolean filterOntologyTerm(Set<String> keywordsFromAttribute, OntologyTerm ontologyTerm, Stemmer stemmer)
	{
		Set<String> ontologyTermSynonyms = semanticSearchServiceHelper.getOtLabelAndSynonyms(ontologyTerm);

		for (String synonym : ontologyTermSynonyms)
		{
			Set<String> splitIntoTerms = splitIntoTerms(stemmer.stemAndJoin(splitIntoTerms(synonym)));
			if (splitIntoTerms.size() != 0 && keywordsFromAttribute.containsAll(splitIntoTerms)) return true;
		}

		return false;
	}

	/**
	 * Computes the best matching synonym which is closest to a set of search terms.<br/>
	 * Will stem the {@link OntologyTerm} 's synonyms and the search terms, and then compute the maximum
	 * {@link StringDistance} between them. 0 means disjunct, 1 means identical
	 * 
	 * @param ontologyTerm
	 *            the {@link OntologyTerm}
	 * @param searchTerms
	 *            the search terms
	 * @return the maximum {@link StringDistance} between the ontologyterm and the search terms
	 */
	public Hit<String> bestMatchingSynonym(OntologyTerm ontologyTerm, Set<String> searchTerms)
	{
		Stemmer stemmer = new Stemmer();
		Optional<Hit<String>> bestSynonym = ontologyTerm.getSynonyms().stream()
				.map(synonym -> Hit.<String> create(synonym, distanceFrom(synonym, searchTerms, stemmer)))
				.max(Comparator.naturalOrder());
		return bestSynonym.get();
	}

	float distanceFrom(String synonym, Set<String> searchTerms, Stemmer stemmer)
	{
		String s1 = stemmer.stemAndJoin(splitIntoTerms(synonym));
		String s2 = stemmer.stemAndJoin(searchTerms);
		float distance = (float) NGramDistanceAlgorithm.stringMatching(s1, s2) / 100;
		LOG.debug("Similarity between: {} and {} is {}", s1, s2, distance);
		return distance;
	}

	private Set<String> splitIntoTerms(String description)
	{
		return FluentIterable.from(termSplitter.split(description)).transform(String::toLowerCase)
				.filter(w -> !NGramDistanceAlgorithm.STOPWORDSLIST.contains(w)).filter(w -> !StringUtils.isEmpty(w))
				.toSet();
	}
}
