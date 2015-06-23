package org.molgenis.data.semanticsearch.service.impl;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.elasticsearch.common.collect.Sets;
import org.mockito.Mockito;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.meta.AttributeMetaDataMetaData;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.semanticsearch.explain.service.ElasticSearchExplainService;
import org.molgenis.data.semanticsearch.semantic.Hit;
import org.molgenis.data.semanticsearch.service.OntologyTagService;
import org.molgenis.data.semanticsearch.service.SemanticSearchService;
import org.molgenis.data.semanticsearch.string.Stemmer;
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
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
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
	private SemanticSearchServiceImpl semanticSearchService;

	private List<String> ontologies;

	private OntologyTerm standingHeight;

	private OntologyTerm bodyWeight;

	private OntologyTerm hypertension;

	private OntologyTerm maternalHypertension;

	private List<OntologyTerm> ontologyTerms;

	private DefaultAttributeMetaData attribute;

	@BeforeTest
	public void beforeTest()
	{
		ontologies = asList("1", "2");
		standingHeight = OntologyTerm.create("http://onto/height", "Standing height",
				Arrays.asList("Standing height", "length"));
		bodyWeight = OntologyTerm.create("http://onto/bmi", "Body weight",
				Arrays.asList("Body weight", "Mass in kilograms"));

		hypertension = OntologyTerm.create("http://onto/hyp", "Hypertension");
		maternalHypertension = OntologyTerm.create("http://onto/mhyp", "Maternal hypertension");
		ontologyTerms = asList(standingHeight, bodyWeight, hypertension, maternalHypertension);
		attribute = new DefaultAttributeMetaData("attr1");
	}

	@BeforeMethod
	public void init()
	{
		when(semanticSearchServiceHelper.getOtLabelAndSynonyms(standingHeight)).thenReturn(
				Sets.newHashSet("Standing height", "Standing height", "length"));

		when(semanticSearchServiceHelper.getOtLabelAndSynonyms(bodyWeight)).thenReturn(
				Sets.newHashSet("Body weight", "Body weight", "Mass in kilograms"));

		when(semanticSearchServiceHelper.getOtLabelAndSynonyms(hypertension)).thenReturn(
				Sets.newHashSet("Hypertension"));

		when(semanticSearchServiceHelper.getOtLabelAndSynonyms(maternalHypertension)).thenReturn(
				Sets.newHashSet("Maternal hypertension"));
	}

	@Test
	public void testSearchHypertension() throws InterruptedException, ExecutionException
	{
		Mockito.reset(ontologyService);
		attribute.setDescription("History of Hypertension");
		when(ontologyService.findOntologyTerms(ontologies, ImmutableSet.<String> of("history", "hypertens"), 100))
				.thenReturn(ontologyTerms);
		Hit<OntologyTerm> result = semanticSearchService.findTags(attribute, ontologies);
		assertEquals(result, null);
	}

	@Test
	public void testDistanceFrom()
	{
		Stemmer stemmer = new Stemmer();
		Assert.assertEquals(semanticSearchService.distanceFrom("Hypertension",
				ImmutableSet.<String> of("history", "hypertens"), stemmer), .6923, 0.0001,
				"String distance should be equal");
		Assert.assertEquals(
				semanticSearchService.distanceFrom("Maternal Hypertension",
						ImmutableSet.<String> of("history", "hypertens"), stemmer), .5454, 0.0001,
				"String distance should be equal");
		;
	}

	@Test
	public void testSearchDescription() throws InterruptedException, ExecutionException
	{
		Mockito.reset(ontologyService);
		attribute.setDescription("Standing height in meters.");
		when(
				ontologyService.findOntologyTerms(ontologies, ImmutableSet.<String> of("standing", "height", "meters"),
						100)).thenReturn(ontologyTerms);
		Hit<OntologyTerm> result = semanticSearchService.findTags(attribute, ontologies);
		assertEquals(result, Hit.<OntologyTerm> create(standingHeight, 0.81250f));
	}

	@Test
	public void testSearchLabel() throws InterruptedException, ExecutionException
	{
		Mockito.reset(ontologyService);
		attribute.setDescription("Standing height (m.)");

		when(ontologyService.findOntologyTerms(ontologies, ImmutableSet.<String> of("standing", "height", "m"), 100))
				.thenReturn(ontologyTerms);
		Hit<OntologyTerm> result = semanticSearchService.findTags(attribute, ontologies);
		assertEquals(result, Hit.<OntologyTerm> create(standingHeight, 0.92857f));
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
		List<QueryRule> rules = new ArrayList<QueryRule>();
		QueryRule targetQueryRuleLabel = new QueryRule(AttributeMetaDataMetaData.LABEL, Operator.FUZZY_MATCH, "height");
		rules.add(targetQueryRuleLabel);
		QueryRule targetQueryRuleOntologyTermTag = new QueryRule(AttributeMetaDataMetaData.LABEL, Operator.FUZZY_MATCH,
				"standing height");
		rules.add(targetQueryRuleOntologyTermTag);
		QueryRule targetQueryRuleOntologyTermTagSyn = new QueryRule(AttributeMetaDataMetaData.LABEL,
				Operator.FUZZY_MATCH, "length");
		rules.add(targetQueryRuleOntologyTermTagSyn);
		QueryRule disMaxQueryRule = new QueryRule(rules);
		disMaxQueryRule.setOperator(Operator.DIS_MAX);

		when(semanticSearchServiceHelper.createDisMaxQueryRuleForAttribute(targetEntityMetaData, targetAttribute)).thenReturn(
				disMaxQueryRule);

		MapEntity entity1 = new MapEntity(ImmutableMap.of(AttributeMetaDataMetaData.NAME, "height_0",
				AttributeMetaDataMetaData.LABEL, "height", AttributeMetaDataMetaData.DESCRIPTION,
				"this is a height measurement in m!"));
		List<Entity> attributeMetaDataEntities = Arrays.<Entity> asList(entity1);

		List<QueryRule> disMaxQueryRules = Lists.newArrayList(new QueryRule(AttributeMetaDataMetaData.IDENTIFIER,
				Operator.IN, attributeIdentifiers), new QueryRule(Operator.AND), disMaxQueryRule);

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

		Mockito.reset(ontologyService);
		attribute.setDescription("Standing height (Ångstrøm)");

		when(ontologyService.findOntologyTerms(ontologies, ImmutableSet.of("standing", "height", "ångstrøm"), 100))
				.thenReturn(ontologyTerms);
		Hit<OntologyTerm> result = semanticSearchService.findTags(attribute, ontologies);
		assertEquals(result, Hit.<OntologyTerm> create(standingHeight, 0.76471f));
	}

	@Test
	public void testSearchUnicode() throws InterruptedException, ExecutionException
	{
		Mockito.reset(ontologyService);
		attribute.setDescription("/əˈnædrəməs/");

		when(ontologyService.findOntologyTerms(ontologies, ImmutableSet.of("əˈnædrəməs"), 100)).thenReturn(
				ontologyTerms);
		Hit<OntologyTerm> result = semanticSearchService.findTags(attribute, ontologies);
		assertEquals(result, null);
	}

	@Test
	public void testSearchMultipleTags() throws InterruptedException, ExecutionException
	{
		Mockito.reset(ontologyService);
		attribute.setDescription("Body mass index");

		when(ontologyService.findOntologyTerms(ontologies, ImmutableSet.of("body", "mass", "index"), 100)).thenReturn(
				ontologyTerms);
		Hit<OntologyTerm> result = semanticSearchService.findTags(attribute, ontologies);
		assertEquals(result, null);
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
		ElasticSearchExplainService elasticSearchExplainService()
		{
			return mock(ElasticSearchExplainService.class);
		}

		@Bean
		SemanticSearchServiceHelper semanticSearchServiceHelper()
		{
			return mock(SemanticSearchServiceHelper.class);
		}
	}
}
