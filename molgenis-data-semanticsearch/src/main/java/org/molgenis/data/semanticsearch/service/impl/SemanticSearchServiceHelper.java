package org.molgenis.data.semanticsearch.service.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.stream;
import static org.molgenis.data.semanticsearch.string.NGramDistanceAlgorithm.STOPWORDSLIST;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.meta.AttributeMetaDataMetaData;
import org.molgenis.data.meta.EntityMetaDataMetaData;
import org.molgenis.data.semanticsearch.string.Stemmer;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.ontology.core.service.OntologyService;
import org.molgenis.ontology.ic.TermFrequencyService;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Sets;

public class SemanticSearchServiceHelper
{
	private final TermFrequencyService termFrequencyService;

	private final DataService dataService;

	private final OntologyService ontologyService;

	private final Stemmer stemmer = new Stemmer();

	public static final int MAX_NUM_TAGS = 3;

	@Autowired
	public SemanticSearchServiceHelper(DataService dataService, OntologyService ontologyService,
			TermFrequencyService termFrequencyService)
	{
		this.dataService = checkNotNull(dataService);
		this.ontologyService = checkNotNull(ontologyService);
		this.termFrequencyService = checkNotNull(termFrequencyService);
	}

	/**
	 * Create a disMaxJunc query rule based on the given search terms as well as the information from given ontology
	 * terms
	 * 
	 * @param ontologyTerms
	 * @param searchTerms
	 * 
	 * @return disMaxJunc queryRule
	 */
	public QueryRule createDisMaxQueryRuleForAttribute(Set<String> searchTerms, Collection<OntologyTerm> ontologyTerms)
	{
		List<String> queryTerms = new ArrayList<String>();

		if (searchTerms != null)
		{
			searchTerms.stream().filter(searchTerm -> StringUtils.isNotBlank(searchTerm))
					.forEach(searchTerm -> queryTerms.add(parseQueryString(searchTerm)));
		}

		// Handle tags with only one ontologyterm
		ontologyTerms.stream().filter(ontologyTerm -> !ontologyTerm.getIRI().contains(",")).forEach(ot -> {
			queryTerms.addAll(parseOntologyTermQueries(ot));
		});

		QueryRule disMaxQueryRule = createDisMaxQueryRuleForTerms(queryTerms);

		// Handle tags with multiple ontologyterms
		ontologyTerms.stream().filter(ontologyTerm -> ontologyTerm.getIRI().contains(",")).forEach(ot -> {
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
		queryTerms.stream().filter(query -> StringUtils.isNotEmpty(query)).map(QueryParser::escape)
				.map(this::reverseEscapeLuceneChar).forEach(query -> {
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
	public QueryRule createDisMaxQueryRuleForTermsWithBoost(List<String> queryTerms, Double boostValue)
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
		for (String ontologyTermIri : multiOntologyTermIri.split(","))
		{
			OntologyTerm ontologyTerm = ontologyService.getOntologyTerm(ontologyTermIri);
			List<String> queryTerms = parseOntologyTermQueries(ontologyTerm);
			Double termFrequency = termFrequencyService.getTermFrequency(ontologyTerm.getLabel());
			shouldQueryRule.getNestedRules().add(createDisMaxQueryRuleForTermsWithBoost(queryTerms, termFrequency));
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
		List<String> queryTerms = getOtLabelAndSynonyms(ontologyTerm).stream().map(term -> parseQueryString(term))
				.collect(Collectors.<String> toList());

		for (OntologyTerm childOt : ontologyService.getChildren(ontologyTerm))
		{
			double boostedNumber = Math.pow(0.5, ontologyService.getOntologyTermDistance(ontologyTerm, childOt));
			getOtLabelAndSynonyms(childOt).forEach(
					synonym -> queryTerms.add(parseBoostQueryString(synonym, boostedNumber)));
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
				.forEach(queryTerm -> expandedQueryMap.put(stemmer.cleanStemPhrase(queryTerm), queryTerm));

		for (OntologyTerm ontologyTerm : ontologyTerms)
		{
			if (!ontologyTerm.getIRI().contains(","))
			{
				collectOntologyTermQueryMap(expandedQueryMap, ontologyTerm);
			}
			else
			{
				for (String ontologyTermIri : ontologyTerm.getIRI().split(","))
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
			getOtLabelAndSynonyms(ontologyTerm).forEach(
					term -> expanedQueryMap.put(stemmer.cleanStemPhrase(term), ontologyTerm.getLabel()));

			for (OntologyTerm childOntologyTerm : ontologyService.getChildren(ontologyTerm))
			{
				getOtLabelAndSynonyms(childOntologyTerm).forEach(
						term -> expanedQueryMap.put(stemmer.cleanStemPhrase(term), ontologyTerm.getLabel()));
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
		Entity entityMetaDataEntity = dataService.findOne(EntityMetaDataMetaData.ENTITY_NAME,
				new QueryImpl().eq(EntityMetaDataMetaData.FULL_NAME, sourceEntityMetaData.getName()));

		if (entityMetaDataEntity == null) throw new MolgenisDataAccessException(
				"Could not find EntityMetaDataEntity by the name of " + sourceEntityMetaData.getName());

		return FluentIterable.from(entityMetaDataEntity.getEntities(EntityMetaDataMetaData.ATTRIBUTES))
				.transform(new Function<Entity, String>()
				{
					public String apply(Entity attributeEntity)
					{
						return attributeEntity.getString(AttributeMetaDataMetaData.IDENTIFIER);
					}
				}).toList();
	}

	public List<OntologyTerm> findTags(String description, List<String> ontologyIds)
	{
		Set<String> searchTerms = removeStopWords(description);

		List<OntologyTerm> matchingOntologyTerms = ontologyService.findOntologyTerms(ontologyIds, searchTerms,
				MAX_NUM_TAGS);

		return matchingOntologyTerms;
	}

	public String parseQueryString(String queryString)
	{
		return StringUtils.join(removeStopWords(queryString), ' ');
	}

	public String parseBoostQueryString(String queryString, double boost)
	{
		return StringUtils.join(
				removeStopWords(queryString).stream().map(word -> word + "^" + boost).collect(Collectors.toSet()), ' ');
	}

	public String reverseEscapeLuceneChar(String string)
	{
		return string.replace("\\^", "^");
	}

	public Set<String> removeStopWords(String description)
	{
		String regex = "[^\\p{L}'a-zA-Z0-9\\.~^]+";
		Set<String> searchTerms = stream(description.split(regex)).map(String::toLowerCase)
				.filter(w -> !STOPWORDSLIST.contains(w) && StringUtils.isNotEmpty(w)).collect(Collectors.toSet());
		return searchTerms;
	}
}
