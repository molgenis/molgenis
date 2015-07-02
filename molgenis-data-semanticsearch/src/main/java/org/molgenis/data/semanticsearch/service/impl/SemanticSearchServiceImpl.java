package org.molgenis.data.semanticsearch.service.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
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
import org.molgenis.data.meta.MetaUtils;
import org.molgenis.data.semanticsearch.explain.bean.ExplainedQueryString;
import org.molgenis.data.semanticsearch.explain.service.ElasticSearchExplainService;
import org.molgenis.data.semanticsearch.semantic.Hit;
import org.molgenis.data.semanticsearch.service.OntologyTagService;
import org.molgenis.data.semanticsearch.service.SemanticSearchService;
import org.molgenis.data.semanticsearch.string.NGramDistanceAlgorithm;
import org.molgenis.data.semanticsearch.string.Stemmer;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.ontology.core.service.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import autovalue.shaded.com.google.common.common.collect.Sets;

import com.google.common.base.Splitter;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Ordering;

public class SemanticSearchServiceImpl implements SemanticSearchService
{
	public static final int MAX_NUM_TAGS = 100;

	private static final Logger LOG = LoggerFactory.getLogger(SemanticSearchServiceImpl.class);

	@Autowired
	private OntologyService ontologyService;

	@Autowired
	private MetaDataService metaDataService;

	@Autowired
	private DataService dataService;

	@Autowired
	private OntologyTagService ontologyTagService;

	@Autowired
	private SemanticSearchServiceHelper semanticSearchServiceHelper;

	@Autowired
	private ElasticSearchExplainService elasticSearchExplainService;

	private static final float CUTOFF = 0.4f;

	private Splitter termSplitter = Splitter.onPattern("[^\\p{IsAlphabetic}]+");
	private Joiner termJoiner = Joiner.on(' ');

	@Override
	public Iterable<AttributeMetaData> findAttributes(EntityMetaData sourceEntityMetaData,
			EntityMetaData targetEntityMetaData, AttributeMetaData targetAttribute)
	{
		Iterable<String> attributeIdentifiers = semanticSearchServiceHelper
				.getAttributeIdentifiers(sourceEntityMetaData);

		QueryRule createDisMaxQueryRule = semanticSearchServiceHelper.createDisMaxQueryRuleForAttribute(
				targetEntityMetaData, targetAttribute);

		List<QueryRule> disMaxQueryRules = Lists.newArrayList(new QueryRule(AttributeMetaDataMetaData.IDENTIFIER,
				Operator.IN, attributeIdentifiers));

		if (createDisMaxQueryRule.getNestedRules().size() > 0)
		{
			disMaxQueryRules.addAll(Arrays.asList(new QueryRule(Operator.AND), createDisMaxQueryRule));
		}

		Iterable<Entity> attributeMetaDataEntities = dataService.findAll(AttributeMetaDataMetaData.ENTITY_NAME,
				new QueryImpl(disMaxQueryRules));

		return MetaUtils.toExistingAttributeMetaData(sourceEntityMetaData, attributeMetaDataEntities);
	}

	// TODO : remove the findAttributes method later on because of the duplicated code
	public Map<AttributeMetaData, Iterable<ExplainedQueryString>> explainAttributes(
			EntityMetaData sourceEntityMetaData, EntityMetaData targetEntityMetaData, AttributeMetaData targetAttribute)
	{
		Iterable<String> attributeIdentifiers = semanticSearchServiceHelper
				.getAttributeIdentifiers(sourceEntityMetaData);

		QueryRule disMaxQueryRule = semanticSearchServiceHelper.createDisMaxQueryRuleForAttribute(targetEntityMetaData,
				targetAttribute);

		List<QueryRule> finalQueryRules = Lists.newArrayList(new QueryRule(AttributeMetaDataMetaData.IDENTIFIER,
				Operator.IN, attributeIdentifiers));

		if (disMaxQueryRule.getNestedRules().size() > 0)
		{
			finalQueryRules.addAll(Arrays.asList(new QueryRule(Operator.AND), disMaxQueryRule));
		}

		Iterable<Entity> attributeMetaDataEntities = dataService.findAll(AttributeMetaDataMetaData.ENTITY_NAME,
				new QueryImpl(finalQueryRules));

		Map<String, String> collectExpanedQueryMap = semanticSearchServiceHelper.collectExpandedQueryMap(
				targetEntityMetaData, targetAttribute);

		// Because the explain-API can be computationally expensive we limit the explanation to the top 10 attributes
		Map<AttributeMetaData, Iterable<ExplainedQueryString>> explainedAttributes = new LinkedHashMap<AttributeMetaData, Iterable<ExplainedQueryString>>();
		int count = 0;
		for (Entity attributeEntity : attributeMetaDataEntities)
		{
			AttributeMetaData attribute = sourceEntityMetaData.getAttribute(attributeEntity
					.getString(AttributeMetaDataMetaData.NAME));
			if (count < 10)
			{
				explainedAttributes.put(
						attribute,
						convertAttributeEntityToExplainedAttribute(attributeEntity, sourceEntityMetaData,
								collectExpanedQueryMap, finalQueryRules));
			}
			else
			{

				explainedAttributes.put(attribute, Collections.emptySet());
			}
			count++;
		}

		return explainedAttributes;
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

		Set<ExplainedQueryString> detectedQueryStrings = elasticSearchExplainService.findQueriesFromExplanation(
				collectExpanedQueryMap, explanation);

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

		List<Hit<OntologyTerm>> hits = candidates
				.stream()
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
