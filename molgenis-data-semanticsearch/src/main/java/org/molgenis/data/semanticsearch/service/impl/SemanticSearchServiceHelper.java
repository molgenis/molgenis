package org.molgenis.data.semanticsearch.service.impl;

import static java.util.Arrays.stream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.meta.AttributeMetaDataMetaData;
import org.molgenis.data.meta.EntityMetaDataMetaData;
import org.molgenis.data.semantic.Relation;
import org.molgenis.data.semanticsearch.service.OntologyTagService;
import org.molgenis.data.semanticsearch.string.Stemmer;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.ontology.core.service.OntologyService;
import org.molgenis.ontology.ic.TermFrequencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

@Component
public class SemanticSearchServiceHelper
{
	private final OntologyTagService ontologyTagService;

	private final TermFrequencyService termFrequencyService;

	private final DataService dataService;

	private final OntologyService ontologyService;

	private final Stemmer stemmer = new Stemmer();

	public static final Set<String> STOP_WORDS;

	public static final int MAX_NUM_TAGS = 3;

	static
	{
		STOP_WORDS = new HashSet<String>(Arrays.asList("a", "you", "about", "above", "after", "again", "against",
				"all", "am", "an", "and", "any", "are", "aren't", "as", "at", "be", "because", "been", "before",
				"being", "below", "between", "both", "but", "by", "can't", "cannot", "could", "couldn't", "did",
				"didn't", "do", "does", "doesn't", "doing", "don't", "down", "during", "each", "few", "for", "from",
				"further", "had", "hadn't", "has", "hasn't", "have", "haven't", "having", "he", "he'd", "he'll",
				"he's", "her", "here", "here's", "hers", "herself", "him", "himself", "his", "how", "how's", "i",
				"i'd", "i'll", "i'm", "i've", "if", "in", "into", "is", "isn't", "it", "it's", "its", "itself",
				"let's", "me", "more", "most", "mustn't", "my", "myself", "no", "nor", "not", "of", "off", "on",
				"once", "only", "or", "other", "ought", "our", "ours", "ourselves", "out", "over", "own", "same",
				"shan't", "she", "she'd", "she'll", "she's", "should", "shouldn't", "so", "some", "such", "than",
				"that", "that's", "the", "their", "theirs", "them", "themselves", "then", "there", "there's", "these",
				"they", "they'd", "they'll", "they're", "they've", "this", "those", "through", "to", "too", "under",
				"until", "up", "very", "was", "wasn't", "we", "we'd", "we'll", "we're", "we've", "were", "weren't",
				"what", "what's", "when", "when's", "where", "where's", "which", "while", "who", "who's", "whom",
				"why", "why's", "with", "won't", "would", "wouldn't", "you", "you'd", "you'll", "you're", "you've",
				"your", "yours", "yourself", "yourselves", "many", ")", "("));
	}

	@Autowired
	public SemanticSearchServiceHelper(OntologyTagService ontologyTagService, DataService dataService,
			OntologyService ontologyService, TermFrequencyService termFrequencyService)
	{
		if (null == ontologyTagService || null == dataService || null == ontologyService
				|| null == termFrequencyService) throw new MolgenisDataException(
				"Service is not found, please contact your application administrator");

		this.dataService = dataService;
		this.ontologyTagService = ontologyTagService;
		this.ontologyService = ontologyService;
		this.termFrequencyService = termFrequencyService;
	}

	/**
	 * Create a disMaxJunc query rule based on the label and description from target attribute as well as the
	 * information from ontology term tags
	 * 
	 * @param targetEntityMetaData
	 * @param targetAttribute
	 * @return disMaxJunc queryRule
	 */
	public QueryRule createDisMaxQueryRuleForAttribute(EntityMetaData targetEntityMetaData,
			AttributeMetaData targetAttribute)
	{
		List<String> queryTerms = new ArrayList<String>();

		if (StringUtils.isNotEmpty(targetAttribute.getLabel()))
		{
			queryTerms.add(parseQueryString(targetAttribute.getLabel()));
		}

		if (StringUtils.isNotEmpty(targetAttribute.getDescription()))
		{
			queryTerms.add(parseQueryString(targetAttribute.getDescription()));
		}

		Multimap<Relation, OntologyTerm> tagsForAttribute = ontologyTagService.getTagsForAttribute(
				targetEntityMetaData, targetAttribute);

		// Handle tags with only one ontologyterm
		tagsForAttribute.values().stream().filter(ontologyTerm -> !ontologyTerm.getIRI().contains(",")).forEach(ot -> {
			queryTerms.addAll(parseOntologyTermQueries(ot));
		});

		QueryRule disMaxQueryRule = createDisMaxQueryRuleForTerms(queryTerms);

		// Handle tags with multiple ontologyterms
		tagsForAttribute.values().stream().filter(ontologyTerm -> ontologyTerm.getIRI().contains(",")).forEach(ot -> {
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

	/**
	 * This function creates a map that contains the expanded query as key and original tag label as the value. This map
	 * allows us to trace back which tags are used in matching
	 * 
	 * @param targetEntityMetaData
	 * @param targetAttribute
	 * @return
	 */
	public Map<String, String> collectExpandedQueryMap(EntityMetaData targetEntityMetaData,
			AttributeMetaData targetAttribute)
	{
		Map<String, String> expandedQueryMap = new LinkedHashMap<String, String>();

		if (StringUtils.isNotEmpty(targetAttribute.getLabel()))
		{
			expandedQueryMap.put(stemmer.cleanStemPhrase(targetAttribute.getLabel()), targetAttribute.getLabel());
		}

		if (StringUtils.isNotEmpty(targetAttribute.getDescription()))
		{
			expandedQueryMap.put(stemmer.cleanStemPhrase(targetAttribute.getDescription()),
					targetAttribute.getDescription());
		}

		for (OntologyTerm ontologyTerm : ontologyTagService.getTagsForAttribute(targetEntityMetaData, targetAttribute)
				.values())
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
				.filter(w -> !STOP_WORDS.contains(w) && StringUtils.isNotEmpty(w)).collect(Collectors.toSet());
		return searchTerms;
	}
}
