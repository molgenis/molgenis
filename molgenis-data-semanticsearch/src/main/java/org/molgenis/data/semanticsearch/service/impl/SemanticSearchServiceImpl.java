package org.molgenis.data.semanticsearch.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.collect.Lists;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.AttributeMetaDataMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.EntityMetaDataMetaData;
import org.molgenis.data.semanticsearch.explain.bean.ExplainedMatchCandidate;
import org.molgenis.data.semanticsearch.explain.service.ExplainMappingService;
import org.molgenis.data.semanticsearch.semantic.Hit;
import org.molgenis.data.semanticsearch.service.QueryExpansionService;
import org.molgenis.data.semanticsearch.service.SemanticSearchService;
import org.molgenis.data.semanticsearch.service.TagGroupGenerator;
import org.molgenis.data.semanticsearch.service.bean.SearchParam;
import org.molgenis.data.semanticsearch.service.bean.TagGroup;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.core.model.Ontology;
import org.molgenis.ontology.core.model.OntologyTagObject;
import org.molgenis.ontology.core.service.OntologyService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.meta.model.AttributeMetaDataMetaData.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.EntityMetaDataMetaData.ENTITY_META_DATA;

public class SemanticSearchServiceImpl implements SemanticSearchService
{
	private final DataService dataService;
	private final OntologyService ontologyService;
	private final TagGroupGenerator tagGroupGenerator;
	private final QueryExpansionService queryExpansionService;
	private final ExplainMappingService explainMappingService;

	private static final String UNIT_ONTOLOGY_IRI = "http://purl.obolibrary.org/obo/uo.owl";

	// We only explain the top 10 suggested attributes because beyond that the attributes are not high quliaty anymore
	private static final int MAX_NUMBER_EXPLAINED_ATTRIBUTES = 10;

	@Autowired
	public SemanticSearchServiceImpl(DataService dataService, OntologyService ontologyService,
			TagGroupGenerator tagGroupGenerator, QueryExpansionService queryExpansionService,
			ExplainMappingService explainMappingService)
	{
		this.dataService = requireNonNull(dataService);
		this.ontologyService = requireNonNull(ontologyService);
		this.tagGroupGenerator = requireNonNull(tagGroupGenerator);
		this.queryExpansionService = requireNonNull(queryExpansionService);
		this.explainMappingService = requireNonNull(explainMappingService);
	}

	@Override
	public Map<AttributeMetaData, ExplainedMatchCandidate<AttributeMetaData>> findAttributes(
			EntityMetaData sourceEntityMetaData, Set<String> queryTerms, List<TagGroup> tagGroups)
	{
		SearchParam searchParam = SearchParam.create(queryTerms, tagGroups);

		QueryRule disMaxQueryRule = queryExpansionService.expand(searchParam);

		Iterable<String> attributeIdentifiers = getAttributeIdentifiers(sourceEntityMetaData);

		List<QueryRule> finalQueryRules = Lists
				.newArrayList(new QueryRule(AttributeMetaDataMetaData.IDENTIFIER, Operator.IN, attributeIdentifiers));

		if (disMaxQueryRule.getNestedRules().size() > 0)
		{
			finalQueryRules.addAll(Arrays.asList(new QueryRule(Operator.AND), disMaxQueryRule));
		}

		Stream<AttributeMetaData> attributeMetaDataEntities = dataService
				.findAll(ATTRIBUTE_META_DATA, new QueryImpl<AttributeMetaData>(finalQueryRules),
						AttributeMetaData.class);

		// Because the explain-API can be computationally expensive we limit the explanation to the top 10 attributes
		Map<AttributeMetaData, ExplainedMatchCandidate<AttributeMetaData>> explainedAttributes = new LinkedHashMap<>();

		AtomicInteger count = new AtomicInteger(0);

		attributeMetaDataEntities.forEach(attribute ->
		{

			if (count.get() < MAX_NUMBER_EXPLAINED_ATTRIBUTES)
			{
				ExplainedMatchCandidate<String> explainedCandidate = explainMappingService
						.explainMapping(searchParam, attribute.getLabel());

				explainedAttributes.put(attribute, ExplainedMatchCandidate
						.create(attribute, explainedCandidate.getExplainedQueryStrings(),
								explainedCandidate.isHighQuality()));
			}
			else
			{
				explainedAttributes.put(attribute, ExplainedMatchCandidate.create(attribute));
			}

			count.incrementAndGet();
		});

		return explainedAttributes;
	}

	@Override
	public Map<AttributeMetaData, ExplainedMatchCandidate<AttributeMetaData>> decisionTreeToFindRelevantAttributes(
			EntityMetaData sourceEntityMetaData, AttributeMetaData targetAttribute,
			Collection<OntologyTagObject> ontologyTagTermsFromTags, Set<String> searchTerms)
	{
		Set<String> queryTerms = createLexicalSearchQueryTerms(targetAttribute, searchTerms);

		List<TagGroup> tagGroups;

		if (null != searchTerms && !searchTerms.isEmpty())
		{
			String queryString = StringUtils.join(searchTerms, " ");
			tagGroups = tagGroupGenerator.generateTagGroups(queryString, ontologyService.getAllOntologiesIds());
		}
		else if (isNull(ontologyTagTermsFromTags) || ontologyTagTermsFromTags.isEmpty())
		{
			List<String> allOntologiesIds = ontologyService.getAllOntologiesIds();
			Ontology unitOntology = ontologyService.getOntology(UNIT_ONTOLOGY_IRI);
			if (unitOntology != null)
			{
				allOntologiesIds.remove(unitOntology.getId());
			}

			tagGroups = tagGroupGenerator.generateTagGroups(targetAttribute.getLabel(), allOntologiesIds);
		}
		else
		{
			tagGroups = ontologyTagTermsFromTags.stream().map(ot -> TagGroup
					.create(ontologyService.getOntologyTerms(ot.getAtomicIRIs()), ot.getLabel(), 1.0f))
					.collect(toList());
		}

		return findAttributes(sourceEntityMetaData, queryTerms, tagGroups);
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

	@Override
	public Map<AttributeMetaData, Hit<OntologyTagObject>> findTags(String entity, List<String> ontologyIds)
	{
		Map<AttributeMetaData, Hit<OntologyTagObject>> result = new LinkedHashMap<>();
		EntityMetaData emd = dataService.getEntityMetaData(entity);
		for (AttributeMetaData amd : emd.getAtomicAttributes())
		{
			List<TagGroup> generateTagGroups = tagGroupGenerator.generateTagGroups(amd.getLabel(), ontologyIds);
			Hit<OntologyTagObject> tag = generateTagGroups.stream()
					.map(tagGroup -> Hit.<OntologyTagObject>create(tagGroup.getCombinedOntologyTerm(),
							tagGroup.getScore())).findFirst().orElse(null);
			if (tag != null)
			{
				result.put(amd, tag);
			}
		}
		return result;
	}

	private List<String> getAttributeIdentifiers(EntityMetaData sourceEntityMetaData)
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
}
