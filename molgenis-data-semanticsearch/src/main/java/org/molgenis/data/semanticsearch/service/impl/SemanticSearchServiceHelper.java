package org.molgenis.data.semanticsearch.service.impl;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.meta.model.AttributeMetaDataMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.EntityMetaDataMetaData;
import org.molgenis.data.semanticsearch.string.NGramDistanceAlgorithm;
import org.molgenis.data.semanticsearch.string.Stemmer;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.ontology.core.service.OntologyService;
import org.molgenis.ontology.ic.TermFrequencyService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.model.EntityMetaDataMetaData.ENTITY_META_DATA;

public class SemanticSearchServiceHelper
{
	private final TermFrequencyService termFrequencyService;

	private final DataService dataService;

	private final OntologyService ontologyService;

	private final Stemmer stemmer = new Stemmer();

	public final static int MAX_NUM_TAGS = 3;

	private final static char SPACE_CHAR = ' ';
	private final static String COMMA_CHAR = ",";
	private final static String CARET_CHARACTER = "^";
	private final static String ESCAPED_CARET_CHARACTER = "\\^";
	private final static String ILLEGAL_CHARS_REGEX = "[^\\p{L}'a-zA-Z0-9\\.~]+";

	@Autowired
	public SemanticSearchServiceHelper(DataService dataService, OntologyService ontologyService,
			TermFrequencyService termFrequencyService)
	{
		this.dataService = requireNonNull(dataService);
		this.ontologyService = requireNonNull(ontologyService);
		this.termFrequencyService = requireNonNull(termFrequencyService);
	}

	/**
	 * Create a disMaxJunc query rule based on the given search terms as well as the information from given ontology
	 * terms
	 *
	 * @param ontologyTerms
	 * @param searchTerms
	 * @return disMaxJunc queryRule
	 */
	public QueryRule createDisMaxQueryRuleForAttribute(Set<String> searchTerms, Collection<OntologyTerm> ontologyTerms)
	{
		List<String> queryTerms = new ArrayList<String>();

		if (searchTerms != null)
		{
			queryTerms.addAll(searchTerms.stream().filter(StringUtils::isNotBlank).map(this::processQueryString)
					.collect(Collectors.toList()));
		}

		// Handle tags with only one ontologyterm
		ontologyTerms.stream().filter(ontologyTerm -> !ontologyTerm.getIRI().contains(COMMA_CHAR)).forEach(ot ->
		{
			queryTerms.addAll(parseOntologyTermQueries(ot));
		});

		QueryRule disMaxQueryRule = createDisMaxQueryRuleForTerms(queryTerms);

		// Handle tags with multiple ontologyterms
		ontologyTerms.stream().filter(ontologyTerm -> ontologyTerm.getIRI().contains(COMMA_CHAR)).forEach(ot ->
		{
			disMaxQueryRule.getNestedRules().add(createShouldQueryRule(ot.getIRI()));
		});

		return disMaxQueryRule;
	}

	/**
	 * Create disMaxJunc query rule based a list of queryTerm. All queryTerms are lower cased and stop words are removed
	 *
	 * @param queryTerms
	 * @return disMaxJunc queryRule
	 */
	public QueryRule createDisMaxQueryRuleForTerms(List<String> queryTerms)
	{
		List<QueryRule> rules = new ArrayList<QueryRule>();
		queryTerms.stream().filter(StringUtils::isNotEmpty).map(this::escapeCharsExcludingCaretChar).forEach(query ->
		{
			rules.add(new QueryRule(AttributeMetaDataMetaData.LABEL, Operator.FUZZY_MATCH, query));
			rules.add(new QueryRule(AttributeMetaDataMetaData.DESCRIPTION, Operator.FUZZY_MATCH, query));
		});
		QueryRule finalDisMaxQuery = new QueryRule(rules);
		finalDisMaxQuery.setOperator(Operator.DIS_MAX);
		return finalDisMaxQuery;
	}

	/**
	 * Create a disMaxQueryRule with corresponding boosted value
	 *
	 * @param queryTerms
	 * @param boostValue
	 * @return a disMaxQueryRule with boosted value
	 */
	public QueryRule createBoostedDisMaxQueryRuleForTerms(List<String> queryTerms, Double boostValue)
	{
		QueryRule finalDisMaxQuery = createDisMaxQueryRuleForTerms(queryTerms);
		if (boostValue != null && boostValue.intValue() != 0)
		{
			finalDisMaxQuery.setValue(boostValue);
		}
		return finalDisMaxQuery;
	}

	/**
	 * Create a boolean should query for composite tags containing multiple ontology terms
	 *
	 * @param multiOntologyTermIri
	 * @return return a boolean should queryRule
	 */
	public QueryRule createShouldQueryRule(String multiOntologyTermIri)
	{
		QueryRule shouldQueryRule = new QueryRule(new ArrayList<QueryRule>());
		shouldQueryRule.setOperator(Operator.SHOULD);
		for (String ontologyTermIri : multiOntologyTermIri.split(COMMA_CHAR))
		{
			OntologyTerm ontologyTerm = ontologyService.getOntologyTerm(ontologyTermIri);
			List<String> queryTerms = parseOntologyTermQueries(ontologyTerm);
			Double termFrequency = getBestInverseDocumentFrequency(queryTerms);
			shouldQueryRule.getNestedRules().add(createBoostedDisMaxQueryRuleForTerms(queryTerms, termFrequency));
		}
		return shouldQueryRule;
	}

	/**
	 * Create a list of string queries based on the information collected from current ontologyterm including label,
	 * synonyms and child ontologyterms
	 *
	 * @param ontologyTerm
	 * @return
	 */
	public List<String> parseOntologyTermQueries(OntologyTerm ontologyTerm)
	{
		List<String> queryTerms = getOtLabelAndSynonyms(ontologyTerm).stream().map(this::processQueryString)
				.collect(Collectors.<String>toList());

		for (OntologyTerm childOt : ontologyService.getChildren(ontologyTerm))
		{
			double boostedNumber = Math.pow(0.5, ontologyService.getOntologyTermDistance(ontologyTerm, childOt));
			getOtLabelAndSynonyms(childOt)
					.forEach(synonym -> queryTerms.add(parseBoostQueryString(synonym, boostedNumber)));
		}
		return queryTerms;
	}

	/**
	 * A helper function to collect synonyms as well as label of ontologyterm
	 *
	 * @param ontologyTerm
	 * @return a list of synonyms plus label
	 */
	public Set<String> getOtLabelAndSynonyms(OntologyTerm ontologyTerm)
	{
		Set<String> allTerms = Sets.newLinkedHashSet(ontologyTerm.getSynonyms());
		allTerms.add(ontologyTerm.getLabel());
		return allTerms;
	}

	public Map<String, String> collectExpandedQueryMap(Set<String> queryTerms, Collection<OntologyTerm> ontologyTerms)
	{
		Map<String, String> expandedQueryMap = new LinkedHashMap<String, String>();

		queryTerms.stream().filter(StringUtils::isNotBlank)
				.forEach(queryTerm -> expandedQueryMap.put(Stemmer.cleanStemPhrase(queryTerm), queryTerm));

		for (OntologyTerm ontologyTerm : ontologyTerms)
		{
			if (!ontologyTerm.getIRI().contains(COMMA_CHAR))
			{
				collectOntologyTermQueryMap(expandedQueryMap, ontologyTerm);
			}
			else
			{
				for (String ontologyTermIri : ontologyTerm.getIRI().split(COMMA_CHAR))
				{
					collectOntologyTermQueryMap(expandedQueryMap, ontologyService.getOntologyTerm(ontologyTermIri));
				}
			}
		}
		return expandedQueryMap;
	}

	public void collectOntologyTermQueryMap(Map<String, String> expanedQueryMap, OntologyTerm ontologyTerm)
	{
		if (ontologyTerm != null)
		{
			getOtLabelAndSynonyms(ontologyTerm)
					.forEach(term -> expanedQueryMap.put(Stemmer.cleanStemPhrase(term), ontologyTerm.getLabel()));

			for (OntologyTerm childOntologyTerm : ontologyService.getChildren(ontologyTerm))
			{
				getOtLabelAndSynonyms(childOntologyTerm)
						.forEach(term -> expanedQueryMap.put(Stemmer.cleanStemPhrase(term), ontologyTerm.getLabel()));
			}
		}
	}

	/**
	 * A helper function that gets identifiers of all the attributes from one entityMetaData
	 *
	 * @param sourceEntityMetaData
	 * @return
	 */
	public List<String> getAttributeIdentifiers(EntityMetaData sourceEntityMetaData)
	{
		Entity entityMetaDataEntity = dataService.findOne(ENTITY_META_DATA,
				new QueryImpl<Entity>().eq(EntityMetaDataMetaData.FULL_NAME, sourceEntityMetaData.getName()));

		if (entityMetaDataEntity == null) throw new MolgenisDataAccessException(
				"Could not find EntityMetaDataEntity by the name of " + sourceEntityMetaData.getName());

		List<String> attributeIdentifiers = new ArrayList<String>();

		recursivelyCollectAttributeIdentifiers(entityMetaDataEntity.getEntities(EntityMetaDataMetaData.ATTRIBUTES),
				attributeIdentifiers);

		return attributeIdentifiers;
	}

	private void recursivelyCollectAttributeIdentifiers(Iterable<Entity> attributeEntities,
			List<String> attributeIdentifiers)
	{
		for (Entity attributeEntity : attributeEntities)
		{
			if (!attributeEntity.getString(AttributeMetaDataMetaData.DATA_TYPE)
					.equals(MolgenisFieldTypes.COMPOUND.toString()))
			{
				attributeIdentifiers.add(attributeEntity.getString(AttributeMetaDataMetaData.IDENTIFIER));
			}
			Iterable<Entity> entities = attributeEntity.getEntities(AttributeMetaDataMetaData.PARTS);

			if (entities != null)
			{
				recursivelyCollectAttributeIdentifiers(entities, attributeIdentifiers);
			}
		}
	}

	public List<OntologyTerm> findTags(String description, List<String> ontologyIds)
	{
		Set<String> searchTerms = removeStopWords(description);

		List<OntologyTerm> matchingOntologyTerms = ontologyService
				.findOntologyTerms(ontologyIds, searchTerms, MAX_NUM_TAGS);

		return matchingOntologyTerms;
	}

	public String processQueryString(String queryString)
	{
		return StringUtils.join(removeStopWords(queryString), SPACE_CHAR);
	}

	public String parseBoostQueryString(String queryString, double boost)
	{
		return StringUtils.join(removeStopWords(queryString).stream().map(word -> word + CARET_CHARACTER + boost)
				.collect(Collectors.toSet()), SPACE_CHAR);
	}

	public String escapeCharsExcludingCaretChar(String string)
	{
		return QueryParser.escape(string).replace(ESCAPED_CARET_CHARACTER, CARET_CHARACTER);
	}

	public Set<String> removeStopWords(String description)
	{
		Set<String> searchTerms = stream(description.split(ILLEGAL_CHARS_REGEX)).map(String::toLowerCase)
				.filter(w -> !NGramDistanceAlgorithm.STOPWORDSLIST.contains(w) && StringUtils.isNotEmpty(w))
				.collect(Collectors.toSet());
		return searchTerms;
	}

	private Double getBestInverseDocumentFrequency(List<String> terms)
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
}
