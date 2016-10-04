package org.molgenis.data.semanticsearch.service.impl;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.model.AttributeMetaDataMetaData.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.EntityMetaDataMetaData.ENTITY_META_DATA;
import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.molgenis.MolgenisFieldTypes.AttributeType;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.QueryRule;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.AttributeMetaDataMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.data.meta.model.EntityMetaDataMetaData;
import org.molgenis.data.semanticsearch.explain.bean.ExplainedMatchCandidate;
import org.molgenis.data.semanticsearch.explain.bean.ExplainedQueryString;
import org.molgenis.data.semanticsearch.explain.service.ExplainMappingService;
import org.molgenis.data.semanticsearch.service.OntologyTagService;
import org.molgenis.data.semanticsearch.service.QueryExpansionService;
import org.molgenis.data.semanticsearch.service.SemanticSearchService;
import org.molgenis.data.semanticsearch.service.TagGroupGenerator;
import org.molgenis.data.semanticsearch.service.bean.SearchParam;
import org.molgenis.data.semanticsearch.service.bean.TagGroup;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.core.model.OntologyTermImpl;
import org.molgenis.ontology.core.service.OntologyService;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@ContextConfiguration(classes = SemanticSearchServiceImplTest.Config.class)
public class SemanticSearchServiceImplTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private EntityMetaDataFactory entityMetaFactory;

	@Autowired
	private AttributeMetaDataFactory attrMetaDataFactory;

	@Autowired
	TagGroupGenerator tagGroupGenerator;

	@Autowired
	QueryExpansionService queryExpansionService;

	@Autowired
	ExplainMappingService explainMappingService;

	@Autowired
	private DataService dataService;

	@Autowired
	private SemanticSearchServiceImpl semanticSearchService;

	@Test
	public void testFindAttributes()
	{
		EntityMetaData sourceEntityMetaData = entityMetaFactory.create().setSimpleName("sourceEntityMetaData");
		AttributeMetaData sourceAttributeHeight = attrMetaDataFactory.create().setName("height_0")
				.setLabel("height in cm");
		AttributeMetaData sourceAttributeWeight = attrMetaDataFactory.create().setName("weight_0")
				.setLabel("weight in kg");
		sourceEntityMetaData.addAttribute(sourceAttributeHeight);
		sourceEntityMetaData.addAttribute(sourceAttributeWeight);

		// Mock the id's of the attribute entities that should be searched
		Entity sourceEntity1 = mock(Entity.class);
		when(sourceEntity1.getString(AttributeMetaDataMetaData.IDENTIFIER)).thenReturn("1");
		when(sourceEntity1.getString(AttributeMetaDataMetaData.NAME)).thenReturn("height_0");
		when(sourceEntity1.getString(AttributeMetaDataMetaData.LABEL)).thenReturn("height");
		when(sourceEntity1.getString(AttributeMetaDataMetaData.DATA_TYPE)).thenReturn(AttributeType.DECIMAL.toString());
		when(sourceEntity1.getString(AttributeMetaDataMetaData.DESCRIPTION))
				.thenReturn("this is a height measurement in m!");

		Entity sourceEntity2 = mock(Entity.class);
		when(sourceEntity2.getString(AttributeMetaDataMetaData.IDENTIFIER)).thenReturn("2");
		when(sourceEntity2.getString(AttributeMetaDataMetaData.NAME)).thenReturn("weight_0");
		when(sourceEntity2.getString(AttributeMetaDataMetaData.LABEL)).thenReturn("weight");
		when(sourceEntity2.getString(AttributeMetaDataMetaData.DATA_TYPE)).thenReturn(AttributeType.DECIMAL.toString());
		when(sourceEntity2.getString(AttributeMetaDataMetaData.DESCRIPTION)).thenReturn("weight measured in kg");

		Entity sourceEntityMetaDataEntity = mock(Entity.class);

		when(sourceEntityMetaDataEntity.getEntities(EntityMetaDataMetaData.ATTRIBUTES))
				.thenReturn(Arrays.asList(sourceEntity1, sourceEntity2));

		when(dataService.findOne(ENTITY_META_DATA,
				new QueryImpl<Entity>().eq(EntityMetaDataMetaData.FULL_NAME, sourceEntityMetaData.getName())))
						.thenReturn(sourceEntityMetaDataEntity);

		// Mock the ontologyterm
		OntologyTermImpl standingHeight = OntologyTermImpl.create("1", "http://onto/height", "height",
				asList("height", "length"));

		// Mock the search parameter
		List<TagGroup> tagGroups = Arrays.asList(TagGroup.create(Arrays.asList(standingHeight), "height", 0.5f));
		Set<String> lexicalQueries = Sets.newHashSet("Standing height");

		// Mock the createDisMaxQueryRule method
		List<QueryRule> rules = new ArrayList<QueryRule>();
		QueryRule targetQueryRuleLabel = new QueryRule(AttributeMetaDataMetaData.LABEL, QueryRule.Operator.FUZZY_MATCH,
				"standing height");
		rules.add(targetQueryRuleLabel);
		QueryRule targetQueryRuleOntologyTermTag = new QueryRule(AttributeMetaDataMetaData.LABEL,
				QueryRule.Operator.FUZZY_MATCH, "height");
		rules.add(targetQueryRuleOntologyTermTag);
		QueryRule targetQueryRuleOntologyTermTagSyn = new QueryRule(AttributeMetaDataMetaData.LABEL,
				QueryRule.Operator.FUZZY_MATCH, "length");
		rules.add(targetQueryRuleOntologyTermTagSyn);
		QueryRule disMaxQueryRule = new QueryRule(rules);
		disMaxQueryRule.setOperator(QueryRule.Operator.DIS_MAX);

		List<QueryRule> disMaxQueryRules = Lists.newArrayList(
				new QueryRule(AttributeMetaDataMetaData.IDENTIFIER, QueryRule.Operator.IN, Arrays.asList("1", "2")),
				new QueryRule(QueryRule.Operator.AND), disMaxQueryRule);

		// Mock the explainedMatchCandidate
		ExplainedMatchCandidate<String> explainedMatchCandidate = ExplainedMatchCandidate.create(
				sourceAttributeHeight.getLabel(),
				Arrays.asList(ExplainedQueryString.create("height", "height", "height", 0.8f)), true);

		when(queryExpansionService.expand(SearchParam.create(lexicalQueries, tagGroups))).thenReturn(disMaxQueryRule);

		when(explainMappingService.explainMapping(SearchParam.create(lexicalQueries, tagGroups),
				sourceAttributeHeight.getLabel())).thenReturn(explainedMatchCandidate);
		// Case 1
		when(dataService.findAll(ATTRIBUTE_META_DATA, new QueryImpl<AttributeMetaData>(disMaxQueryRules),
				AttributeMetaData.class)).thenReturn(Stream.of(sourceAttributeHeight));

		Map<AttributeMetaData, ExplainedMatchCandidate<AttributeMetaData>> termsActual1 = semanticSearchService
				.findAttributes(sourceEntityMetaData, lexicalQueries, tagGroups);

		Map<AttributeMetaData, ExplainedMatchCandidate<AttributeMetaData>> termsExpected1 = ImmutableMap
				.of(sourceAttributeHeight, ExplainedMatchCandidate.create(sourceAttributeHeight,
						explainedMatchCandidate.getExplainedQueryStrings(), explainedMatchCandidate.isHighQuality()));

		assertEquals(termsActual1.toString(), termsExpected1.toString());

		// Case 2
		when(dataService.findAll(ATTRIBUTE_META_DATA, new QueryImpl<AttributeMetaData>(disMaxQueryRules),
				AttributeMetaData.class)).thenReturn(Stream.empty());

		Map<AttributeMetaData, ExplainedMatchCandidate<AttributeMetaData>> termsActual2 = semanticSearchService
				.findAttributes(sourceEntityMetaData, lexicalQueries, tagGroups);

		Map<AttributeMetaData, ExplainedMatchCandidate<AttributeMetaData>> termsExpected2 = ImmutableMap.of();

		assertEquals(termsActual2, termsExpected2);
	}

	@Configuration
	public static class Config
	{
		@Bean
		MetaDataService metaDataService()
		{
			return mock(MetaDataService.class);
		}

		@Bean
		OntologyService ontologyService()
		{
			return mock(OntologyService.class);
		}

		@Bean
		SemanticSearchService semanticSearchService()
		{
			return new SemanticSearchServiceImpl(dataService(), ontologyService(), metaDataService(),
					tagGroupGenerator(), queryExpansionService(), explainMappingService());
		}

		@Bean
		OntologyTagService ontologyTagService()
		{
			return mock(OntologyTagService.class);
		}

		@Bean
		TagGroupGenerator tagGroupGenerator()
		{
			return mock(TagGroupGenerator.class);
		}

		@Bean
		QueryExpansionService queryExpansionService()
		{
			return mock(QueryExpansionService.class);
		}

		@Bean
		DataService dataService()
		{
			return mock(DataService.class);
		}

		@Bean
		ExplainMappingService explainMappingService()
		{
			return mock(ExplainMappingService.class);
		}
	}
}
