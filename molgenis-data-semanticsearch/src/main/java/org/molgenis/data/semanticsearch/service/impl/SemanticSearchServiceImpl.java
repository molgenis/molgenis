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
import org.elasticsearch.common.collect.Iterables;
import org.elasticsearch.common.collect.Sets;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.Package;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.meta.AttributeMetaDataMetaData;
import org.molgenis.data.meta.EntityMetaDataMetaData;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.MetaUtils;
import org.molgenis.data.semantic.Relation;
import org.molgenis.data.semanticsearch.semantic.ItemizedSearchResult;
import org.molgenis.data.semanticsearch.service.SemanticSearchService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.ontology.core.service.OntologyService;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Multimap;

public class SemanticSearchServiceImpl implements SemanticSearchService
{
	@Autowired
	private OntologyService ontologyService;

	@Autowired
	private MetaDataService metaDataService;

	@Autowired
	private DataService dataService;

	@Autowired
	private OntologyTagService ontologyTagService;

	public static final Set<String> STOP_WORDS;

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
				"once", "only", "or", "other", "ought", "our", "ours ", " ourselves", "out", "over", "own", "same",
				"shan't", "she", "she'd", "she'll", "she's", "should", "shouldn't", "so", "some", "such", "than",
				"that", "that's", "the", "their", "theirs", "them", "themselves", "then", "there", "there's", "these",
				"they", "they'd", "they'll", "they're", "they've", "this", "those", "through", "to", "too", "under",
				"until", "up", "very", "was", "wasn't", "we", "we'd", "we'll", "we're", "we've", "were", "weren't",
				"what", "what's", "when", "when's", "where", "where's", "which", "while", "who", "who's", "whom",
				"why", "why's", "with", "won't", "would", "wouldn't", "you", "you'd", "you'll", "you're", "you've",
				"your", "yours", "yourself", "yourselves", "many", ")", "("));
	}

	@Override
	public Iterable<AttributeMetaData> findAttributes(EntityMetaData sourceEntityMetaData,
			EntityMetaData targetEntityMetaData, AttributeMetaData targetAttribute)
	{
		Iterable<String> attributeIdentifiers = getAttributeIdentifiers(sourceEntityMetaData);

		QueryRule createDisMaxQueryRule = createDisMaxQueryRule(targetEntityMetaData, targetAttribute);

		List<QueryRule> disMaxQueryRules = Arrays.asList(new QueryRule(AttributeMetaDataMetaData.IDENTIFIER,
				Operator.IN, attributeIdentifiers));

		if (createDisMaxQueryRule.getNestedRules().size() > 0)
		{
			disMaxQueryRules.addAll(Arrays.asList(new QueryRule(Operator.AND), createDisMaxQueryRule));
		}

		Iterable<Entity> attributeMetaDataEntities = dataService.findAll(AttributeMetaDataMetaData.ENTITY_NAME,
				new QueryImpl(disMaxQueryRules));

		return Iterables.size(attributeMetaDataEntities) > 0 ? MetaUtils.toAttributeMetaData(sourceEntityMetaData,
				attributeMetaDataEntities) : sourceEntityMetaData.getAttributes();
	}

	private QueryRule createDisMaxQueryRule(EntityMetaData targetEntityMetaData, AttributeMetaData targetAttribute)
	{
		Multimap<Relation, OntologyTerm> tagsForAttribute = ontologyTagService.getTagsForAttribute(
				targetEntityMetaData, targetAttribute);

		List<QueryRule> rules = new ArrayList<QueryRule>();

		// add query rule for searching the label of target attribute in the attribute table.
		if (StringUtils.isNotEmpty(targetAttribute.getDescription()))
		{
			rules.add(new QueryRule(AttributeMetaDataMetaData.LABEL, Operator.FUZZY_MATCH, targetAttribute
					.getDescription()));
		}

		for (OntologyTerm ontologyTerm : tagsForAttribute.values())
		{
			QueryRule disMaxQuery = new QueryRule(new ArrayList<QueryRule>());
			disMaxQuery.setOperator(Operator.DIS_MAX);

			Set<String> synonyms = Sets.newHashSet(ontologyTerm.getSynonyms());
			synonyms.add(ontologyTerm.getLabel());
			for (String synonym : synonyms)
			{
				disMaxQuery.getNestedRules().add(
						new QueryRule(AttributeMetaDataMetaData.LABEL, Operator.FUZZY_MATCH, synonym));
			}

			rules.add(disMaxQuery);
		}

		QueryRule finalDisMaxQuery = new QueryRule(rules);
		finalDisMaxQuery.setOperator(Operator.DIS_MAX);

		return finalDisMaxQuery;
	}

	private Iterable<String> getAttributeIdentifiers(EntityMetaData sourceEntityMetaData)
	{
		Entity entityMetaDataEntity = dataService.findOne(EntityMetaDataMetaData.ENTITY_NAME,
				new QueryImpl().eq(EntityMetaDataMetaData.FULL_NAME, sourceEntityMetaData.getName()));

		if (entityMetaDataEntity == null) throw new MolgenisDataAccessException(
				"Could not find EntityMetaDataEntity by the name of " + sourceEntityMetaData.getName());

		Iterable<String> attributeIdentifiers = FluentIterable.from(
				entityMetaDataEntity.getEntities(EntityMetaDataMetaData.ATTRIBUTES)).transform(
				new Function<Entity, String>()
				{
					public String apply(Entity attributeEntity)
					{
						return attributeEntity.getString(AttributeMetaDataMetaData.IDENTIFIER);
					}
				});
		return attributeIdentifiers;
	}

	@Override
	public Iterable<AttributeMetaData> findAttributes(Package p, AttributeMetaData attributeMetaData)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<ItemizedSearchResult<java.lang.Package>> findPackages(String searchTerm)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<AttributeMetaData, List<OntologyTerm>> findTags(String entity, List<String> ontologyIds)
	{
		Map<AttributeMetaData, List<OntologyTerm>> result = new LinkedHashMap<AttributeMetaData, List<OntologyTerm>>();
		EntityMetaData emd = metaDataService.getEntityMetaData(entity);
		for (AttributeMetaData amd : emd.getAtomicAttributes())
		{
			result.put(amd, findTags(amd, ontologyIds));
		}
		return result;
	}

	@Override
	public List<OntologyTerm> findTags(AttributeMetaData attribute, List<String> ontologyIds)
	{
		String description = attribute.getDescription() == null ? attribute.getLabel() : attribute.getDescription();
		return findTagsSync(description, ontologyIds);
	}

	private List<OntologyTerm> findTagsSync(String description, List<String> ontologyIds)
	{
		Set<String> searchTerms = stream(description.split("\\W+")).map(String::toLowerCase)
				.filter(w -> !STOP_WORDS.contains(w)).collect(Collectors.toSet());

		List<OntologyTerm> matchingOntologyTerms = ontologyService.findOntologyTerms(ontologyIds, searchTerms, 100);

		return matchingOntologyTerms;
	}
}
