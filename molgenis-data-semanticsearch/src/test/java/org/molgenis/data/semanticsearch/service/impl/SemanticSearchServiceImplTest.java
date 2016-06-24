package org.molgenis.data.semanticsearch.service.impl;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import org.elasticsearch.common.collect.Sets;
import org.mockito.Mockito;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.meta.AttributeMetaDataMetaData;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.semanticsearch.explain.bean.ExplainedAttributeMetaData;
import org.molgenis.data.semanticsearch.explain.bean.ExplainedQueryString;
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
	private OntologyService ontologyService;

	@Autowired
	private SemanticSearchServiceHelper semanticSearchServiceHelper;

	@Autowired
	private DataService dataService;

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
		when(semanticSearchServiceHelper.getOtLabelAndSynonyms(standingHeight))
				.thenReturn(Sets.newHashSet("Standing height", "Standing height", "length"));

		when(semanticSearchServiceHelper.getOtLabelAndSynonyms(bodyWeight))
				.thenReturn(Sets.newHashSet("Body weight", "Body weight", "Mass in kilograms"));

		when(semanticSearchServiceHelper.getOtLabelAndSynonyms(hypertension))
				.thenReturn(Sets.newHashSet("Hypertension"));

		when(semanticSearchServiceHelper.getOtLabelAndSynonyms(maternalHypertension))
				.thenReturn(Sets.newHashSet("Maternal hypertension"));
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
						ImmutableSet.<String> of("history", "hypertens"), stemmer),
				.5454, 0.0001, "String distance should be equal");
		;
	}

	@Test
	public void testSearchDescription() throws InterruptedException, ExecutionException
	{
		Mockito.reset(ontologyService);
		attribute.setDescription("Standing height in meters.");
		when(ontologyService.findOntologyTerms(ontologies, ImmutableSet.<String> of("standing", "height", "meters"),
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
	public void testIsSingleMatchHighQuality()
	{
		List<ExplainedQueryString> explanations1 = Arrays
				.asList(ExplainedQueryString.create("height", "height", "standing height", 50.0));
		assertFalse(semanticSearchService.isSingleMatchHighQuality(Sets.newHashSet("height"), Sets.newHashSet("height"),
				explanations1));

		List<ExplainedQueryString> explanations2 = Arrays
				.asList(ExplainedQueryString.create("body length", "body length", "height", 100));

		assertTrue(semanticSearchService.isSingleMatchHighQuality(Sets.newHashSet("height in meter"),
				Sets.newHashSet("height in meter", "height"), explanations2));

		List<ExplainedQueryString> explanations3 = Arrays.asList(
				ExplainedQueryString.create("fasting", "fasting", "fasting", 100),
				ExplainedQueryString.create("glucose", "blood glucose", "blood glucose", 50));

		assertFalse(semanticSearchService.isSingleMatchHighQuality(Sets.newHashSet("fasting glucose"),
				Sets.newHashSet("fasting glucose", "fasting", "blood glucose"), explanations3));

		List<ExplainedQueryString> explanations4 = Arrays
				.asList(ExplainedQueryString.create("number of", "number of", "number", 100));

		assertFalse(semanticSearchService.isSingleMatchHighQuality(Sets.newHashSet("number of cigarette smoked"),
				Sets.newHashSet("number of cigarette smoked", "number of"), explanations4));
	}

	@Test
	public void testIsGoodMatch()
	{
		Map<String, Double> matchedTags = new HashMap<String, Double>();
		matchedTags.put("height", 100.0);
		matchedTags.put("weight", 50.0);
		assertFalse(semanticSearchService.isGoodMatch(matchedTags, "blood"));
		assertFalse(semanticSearchService.isGoodMatch(matchedTags, "weight"));
		assertTrue(semanticSearchService.isGoodMatch(matchedTags, "height"));

		Map<String, Double> matchedTags2 = new HashMap<String, Double>();
		matchedTags2.put("fasting", 100.0);
		matchedTags2.put("glucose", 100.0);

		assertTrue(semanticSearchService.isGoodMatch(matchedTags2, "fasting glucose"));
	}

	@Test
	public void testFindAttributes()
	{
		DefaultEntityMetaData sourceEntityMetaData = new DefaultEntityMetaData("sourceEntityMetaData");

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

		when(semanticSearchServiceHelper.createDisMaxQueryRuleForAttribute(Sets.newHashSet("targetAttribute"),
				Collections.emptyList())).thenReturn(disMaxQueryRule);

		MapEntity entity1 = new MapEntity(
				ImmutableMap.of(AttributeMetaDataMetaData.NAME, "height_0", AttributeMetaDataMetaData.LABEL, "height",
						AttributeMetaDataMetaData.DESCRIPTION, "this is a height measurement in m!"));

		List<QueryRule> disMaxQueryRules = Lists.newArrayList(
				new QueryRule(AttributeMetaDataMetaData.IDENTIFIER, Operator.IN, attributeIdentifiers),
				new QueryRule(Operator.AND), disMaxQueryRule);

		AttributeMetaData attributeHeight = new DefaultAttributeMetaData("height_0");
		AttributeMetaData attributeWeight = new DefaultAttributeMetaData("weight_0");
		sourceEntityMetaData.addAttributeMetaData(attributeHeight);
		sourceEntityMetaData.addAttributeMetaData(attributeWeight);

		// Case 1
		when(dataService.findAll(AttributeMetaDataMetaData.ENTITY_NAME, new QueryImpl(disMaxQueryRules)))
				.thenReturn(Stream.of(entity1));

		Map<AttributeMetaData, ExplainedAttributeMetaData> termsActual1 = semanticSearchService
				.findAttributes(sourceEntityMetaData, Sets.newHashSet("targetAttribute"), Collections.emptyList());

		Map<AttributeMetaData, ExplainedAttributeMetaData> termsExpected1 = ImmutableMap.of(attributeHeight,
				ExplainedAttributeMetaData.create(attributeHeight));

		assertEquals(termsActual1.toString(), termsExpected1.toString());

		// Case 2
		when(dataService.findAll(AttributeMetaDataMetaData.ENTITY_NAME, new QueryImpl(disMaxQueryRules)))
				.thenReturn(Stream.empty());

		Map<AttributeMetaData, ExplainedAttributeMetaData> termsActual2 = semanticSearchService
				.findAttributes(sourceEntityMetaData, Sets.newHashSet("targetAttribute"), Collections.emptyList());

		Map<AttributeMetaData, ExplainedAttributeMetaData> termsExpected2 = ImmutableMap.of();

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

		when(ontologyService.findOntologyTerms(ontologies, ImmutableSet.of("əˈnædrəməs"), 100))
				.thenReturn(ontologyTerms);
		Hit<OntologyTerm> result = semanticSearchService.findTags(attribute, ontologies);
		assertEquals(result, null);
	}

	@Test
	public void testSearchMultipleTags() throws InterruptedException, ExecutionException
	{
		Mockito.reset(ontologyService);
		attribute.setDescription("Body mass index");

		when(ontologyService.findOntologyTerms(ontologies, ImmutableSet.of("body", "mass", "index"), 100))
				.thenReturn(ontologyTerms);
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
			return new SemanticSearchServiceImpl(dataService(), ontologyService(), metaDataService(),
					semanticSearchServiceHelper(), elasticSearchExplainService());
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
