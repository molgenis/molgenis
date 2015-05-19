package org.molgenis.data.semanticsearch.service.impl;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.meta.AttributeMetaDataMetaData;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.semanticsearch.service.OntologyTagService;
import org.molgenis.data.semanticsearch.service.SemanticSearchService;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.ontology.core.service.OntologyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

@ContextConfiguration(classes = SemanticSearchServiceImplTest.Config.class)
public class SemanticSearchServiceImplTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private MetaDataService metaDataService;

	@Autowired
	private OntologyService ontologyService;

	@Autowired
	private SemanticSearchServiceHelper semanticSearchServiceHelper;

	@Autowired
	private DataService dataService;

	@Autowired
	private OntologyTagService ontologyTagService;

	@Autowired
	private SemanticSearchService semanticSearchService;

	private List<String> ontologies;

	private OntologyTerm standingHeight;

	private List<OntologyTerm> ontologyTerms;

	private DefaultAttributeMetaData attribute;

	@BeforeTest
	public void beforeTest()
	{
		ontologies = asList("1", "2");
		standingHeight = OntologyTerm.create("http://onto/height", "Standing height");
		ontologyTerms = asList(standingHeight);
		attribute = new DefaultAttributeMetaData("attr1");
	}

	@Test
	public void testSearchDescription() throws InterruptedException, ExecutionException
	{
		attribute.setDescription("Standing height (m.)");
		when(semanticSearchServiceHelper.findTags("Standing height (m.)", ontologies)).thenReturn(ontologyTerms);
		List<OntologyTerm> terms = semanticSearchService.findTags(attribute, ontologies);
		assertEquals(terms, ontologyTerms);
	}

	@Test
	public void testSearchLabel() throws InterruptedException, ExecutionException
	{
		attribute.setLabel("Standing height (m.)");
		when(semanticSearchServiceHelper.findTags("Standing height (m.)", ontologies)).thenReturn(ontologyTerms);
		List<OntologyTerm> terms = semanticSearchService.findTags(attribute, ontologies);
		assertEquals(terms, ontologyTerms);
	}

	@Test
	public void testFindAttributes()
	{
		DefaultEntityMetaData sourceEntityMetaData = new DefaultEntityMetaData("sourceEntityMetaData");
		EntityMetaData targetEntityMetaData = new DefaultEntityMetaData("targetEntityMetaData");
		DefaultAttributeMetaData targetAttribute = new DefaultAttributeMetaData("targetAttribute");

		// Mock the id's of the attribute entities that should be searched
		List<String> attributeIdentifiers = Arrays.asList("1", "2");
		when(semanticSearchServiceHelper.getAttributeIdentifiers(sourceEntityMetaData))
				.thenReturn(attributeIdentifiers);

		// Mock the createDisMaxQueryRule method
		QueryRule finalDisMaxQueryRule = new QueryRule(new ArrayList<QueryRule>());
		finalDisMaxQueryRule.setOperator(Operator.DIS_MAX);
		QueryRule targetQueryRuleLabel = new QueryRule(AttributeMetaDataMetaData.LABEL, Operator.FUZZY_MATCH, "height");
		finalDisMaxQueryRule.getNestedRules().add(targetQueryRuleLabel);
		QueryRule targetQueryRuleOntologyTermTag = new QueryRule(AttributeMetaDataMetaData.LABEL, Operator.FUZZY_MATCH,
				"standing height");
		QueryRule targetQueryRuleOntologyTermTagSyn = new QueryRule(AttributeMetaDataMetaData.LABEL,
				Operator.FUZZY_MATCH, "length");
		QueryRule disMaxTagQueryRule = new QueryRule(Arrays.asList(targetQueryRuleOntologyTermTag,
				targetQueryRuleOntologyTermTagSyn));
		disMaxTagQueryRule.setOperator(Operator.DIS_MAX);
		finalDisMaxQueryRule.getNestedRules().add(disMaxTagQueryRule);
		when(semanticSearchServiceHelper.createDisMaxQueryRule(targetEntityMetaData, targetAttribute)).thenReturn(
				finalDisMaxQueryRule);

		MapEntity entity1 = new MapEntity(ImmutableMap.of(AttributeMetaDataMetaData.NAME, "height_0",
				AttributeMetaDataMetaData.LABEL, "height", AttributeMetaDataMetaData.DESCRIPTION,
				"this is a height measurement in m!"));
		List<Entity> attributeMetaDataEntities = Arrays.<Entity> asList(entity1);

		List<QueryRule> disMaxQueryRules = Lists.newArrayList(new QueryRule(AttributeMetaDataMetaData.IDENTIFIER,
				Operator.IN, attributeIdentifiers), new QueryRule(Operator.AND), finalDisMaxQueryRule);

		AttributeMetaData attributeHeight = new DefaultAttributeMetaData("height_0");
		AttributeMetaData attributeWeight = new DefaultAttributeMetaData("weight_0");
		sourceEntityMetaData.addAttributeMetaData(attributeHeight);
		sourceEntityMetaData.addAttributeMetaData(attributeWeight);

		// Case 1
		when(dataService.findAll(AttributeMetaDataMetaData.ENTITY_NAME, new QueryImpl(disMaxQueryRules))).thenReturn(
				attributeMetaDataEntities);

		Iterable<AttributeMetaData> termsActual1 = semanticSearchService.findAttributes(sourceEntityMetaData,
				targetEntityMetaData, targetAttribute);

		Iterable<AttributeMetaData> termsExpected1 = Arrays.<AttributeMetaData> asList(attributeHeight);

		assertEquals(termsActual1, termsExpected1);

		// Case 2
		when(dataService.findAll(AttributeMetaDataMetaData.ENTITY_NAME, new QueryImpl(disMaxQueryRules))).thenReturn(
				Arrays.<Entity> asList());

		Iterable<AttributeMetaData> termsActual2 = semanticSearchService.findAttributes(sourceEntityMetaData,
				targetEntityMetaData, targetAttribute);

		Iterable<AttributeMetaData> termsExpected2 = Arrays.<AttributeMetaData> asList();

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
			return new SemanticSearchServiceImpl();
		}

		@Bean
		OntologyTagService ontologyTagService()
		{
			return mock(OntologyTagService.class);
		}

		@Bean
		DataService dataService()
		{
			return mock(DataService.class);
		}

		@Bean
		SemanticSearchServiceHelper semanticSearchServiceHelper()
		{
			return mock(SemanticSearchServiceHelper.class);
		}
	}
}
