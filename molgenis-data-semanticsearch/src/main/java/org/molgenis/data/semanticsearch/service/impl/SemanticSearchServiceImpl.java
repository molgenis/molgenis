package org.molgenis.data.semanticsearch.service.impl;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.lucene.search.Explanation;
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
import org.molgenis.data.semanticsearch.explain.bean.ExplainedAttributeMetaData;
import org.molgenis.data.semanticsearch.explain.service.ElasticSearchExplainService;
import org.molgenis.data.semanticsearch.service.OntologyTagService;
import org.molgenis.data.semanticsearch.service.SemanticSearchService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.ontology.core.service.OntologyService;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.FluentIterable;

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

	@Autowired
	private SemanticSearchServiceHelper semanticSearchServiceHelper;

	@Autowired
	private ElasticSearchExplainService elasticSearchExplainService;

	@Override
	public Iterable<AttributeMetaData> findAttributes(EntityMetaData sourceEntityMetaData,
			EntityMetaData targetEntityMetaData, AttributeMetaData targetAttribute)
	{
		Iterable<String> attributeIdentifiers = semanticSearchServiceHelper
				.getAttributeIdentifiers(sourceEntityMetaData);

		QueryRule createDisMaxQueryRule = semanticSearchServiceHelper.createDisMaxQueryRule(targetEntityMetaData,
				targetAttribute);

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
	public Iterable<ExplainedAttributeMetaData> explainAttributes(EntityMetaData sourceEntityMetaData,
			EntityMetaData targetEntityMetaData, AttributeMetaData targetAttribute)
	{
		Iterable<String> attributeIdentifiers = semanticSearchServiceHelper
				.getAttributeIdentifiers(sourceEntityMetaData);

		QueryRule disMaxQueryRule = semanticSearchServiceHelper.createDisMaxQueryRule(targetEntityMetaData,
				targetAttribute);

		List<QueryRule> finalQueryRules = Lists.newArrayList(new QueryRule(AttributeMetaDataMetaData.IDENTIFIER,
				Operator.IN, attributeIdentifiers));

		if (disMaxQueryRule.getNestedRules().size() > 0)
		{
			finalQueryRules.addAll(Arrays.asList(new QueryRule(Operator.AND), disMaxQueryRule));
		}

		Iterable<Entity> attributeMetaDataEntities = dataService.findAll(AttributeMetaDataMetaData.ENTITY_NAME,
				new QueryImpl(finalQueryRules));

		List<ExplainedAttributeMetaData> explainedAttributes = FluentIterable
				.from(attributeMetaDataEntities)
				.transform(
						entity -> convertAttributeEntityToExplainedAttribute(entity, sourceEntityMetaData,
								disMaxQueryRule, finalQueryRules)).toList();
		return explainedAttributes;
	}

	public ExplainedAttributeMetaData convertAttributeEntityToExplainedAttribute(Entity attributeEntity,
			EntityMetaData sourceEntityMetaData, QueryRule disMaxQueryRule, List<QueryRule> finalQueryRules)
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

		Set<Entry<String, Double>> reverseSearchQueryStrings = elasticSearchExplainService.reverseSearchQueryStrings(
				disMaxQueryRule, explanation);

		return new ExplainedAttributeMetaData(sourceEntityMetaData.getAttribute(attributeName),
				reverseSearchQueryStrings);
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
		return semanticSearchServiceHelper.findTags(description, ontologyIds);
	}
}
