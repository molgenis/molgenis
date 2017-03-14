package org.molgenis.data.semanticsearch.service.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.mockito.Mockito;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.QueryRule;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.*;
import org.molgenis.data.semanticsearch.explain.bean.ExplainedAttribute;
import org.molgenis.data.semanticsearch.explain.bean.ExplainedQueryString;
import org.molgenis.data.semanticsearch.explain.service.ElasticSearchExplainService;
import org.molgenis.data.semanticsearch.semantic.Hit;
import org.molgenis.data.semanticsearch.service.OntologyTagService;
import org.molgenis.data.semanticsearch.string.Stemmer;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.ontology.core.service.OntologyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.testng.Assert.*;

@ContextConfiguration(classes = SemanticSearchServiceImplTest.Config.class)
public class SemanticSearchServiceImplTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private EntityTypeFactory entityTypeFactory;

	@Autowired
	private AttributeFactory attrMetaDataFactory;

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

	private Attribute attribute;

	@BeforeMethod
	public void beforeTest()
	{
		ontologies = asList("1", "2");
		standingHeight = OntologyTerm
				.create("http://onto/height", "Standing height", asList("Standing height", "length"));
		bodyWeight = OntologyTerm.create("http://onto/bmi", "Body weight", asList("Body weight", "Mass in kilograms"));

		hypertension = OntologyTerm.create("http://onto/hyp", "Hypertension");
		maternalHypertension = OntologyTerm.create("http://onto/mhyp", "Maternal hypertension");
		ontologyTerms = asList(standingHeight, bodyWeight, hypertension, maternalHypertension);
		attribute = attrMetaDataFactory.create().setName("attr1");
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
		when(ontologyService.findOntologyTerms(ontologies, ImmutableSet.<String>of("history", "hypertens"), 100))
				.thenReturn(ontologyTerms);
		Hit<OntologyTerm> result = semanticSearchService.findTags(attribute, ontologies);
		assertEquals(result, null);
	}

	@Test
	public void testDistanceFrom()
	{
		Stemmer stemmer = new Stemmer();
		Assert.assertEquals(semanticSearchService
						.distanceFrom("Hypertension", ImmutableSet.<String>of("history", "hypertens"), stemmer), .6923, 0.0001,
				"String distance should be equal");
		Assert.assertEquals(semanticSearchService
						.distanceFrom("Maternal Hypertension", ImmutableSet.<String>of("history", "hypertens"), stemmer), .5454,
				0.0001, "String distance should be equal");
		;
	}

	@Test
	public void testSearchDescription() throws InterruptedException, ExecutionException
	{
		Mockito.reset(ontologyService);
		attribute.setDescription("Standing height in meters.");
		when(ontologyService
				.findOntologyTerms(ontologies, ImmutableSet.<String>of("standing", "height", "meters"), 100))
				.thenReturn(ontologyTerms);
		Hit<OntologyTerm> result = semanticSearchService.findTags(attribute, ontologies);
		assertEquals(result, Hit.<OntologyTerm>create(standingHeight, 0.81250f));
	}

	@Test
	public void testSearchLabel() throws InterruptedException, ExecutionException
	{
		Mockito.reset(ontologyService);
		attribute.setDescription("Standing height (m.)");

		when(ontologyService.findOntologyTerms(ontologies, ImmutableSet.<String>of("standing", "height", "m"), 100))
				.thenReturn(ontologyTerms);
		Hit<OntologyTerm> result = semanticSearchService.findTags(attribute, ontologies);
		assertEquals(result, Hit.<OntologyTerm>create(standingHeight, 0.92857f));
	}

	@Test
	public void testIsSingleMatchHighQuality()
	{
		List<ExplainedQueryString> explanations1 = asList(
				ExplainedQueryString.create("height", "height", "standing height", 50.0));
		assertFalse(semanticSearchService
				.isSingleMatchHighQuality(Sets.newHashSet("height"), Sets.newHashSet("height"), explanations1));

		List<ExplainedQueryString> explanations2 = asList(
				ExplainedQueryString.create("body length", "body length", "height", 100));

		assertTrue(semanticSearchService.isSingleMatchHighQuality(Sets.newHashSet("height in meter"),
				Sets.newHashSet("height in meter", "height"), explanations2));

		List<ExplainedQueryString> explanations3 = asList(
				ExplainedQueryString.create("fasting", "fasting", "fasting", 100),
				ExplainedQueryString.create("glucose", "blood glucose", "blood glucose", 50));

		assertFalse(semanticSearchService.isSingleMatchHighQuality(Sets.newHashSet("fasting glucose"),
				Sets.newHashSet("fasting glucose", "fasting", "blood glucose"), explanations3));

		List<ExplainedQueryString> explanations4 = asList(
				ExplainedQueryString.create("number of", "number of", "number", 100));

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
		EntityType sourceEntityType = entityTypeFactory.create().setName("sourceEntityType");

		// Mock the id's of the attribute entities that should be searched
		List<String> attributeIdentifiers = asList("1", "2");
		when(semanticSearchServiceHelper.getAttributeIdentifiers(sourceEntityType)).thenReturn(attributeIdentifiers);

		// Mock the createDisMaxQueryRule method
		List<QueryRule> rules = new ArrayList<QueryRule>();
		QueryRule targetQueryRuleLabel = new QueryRule(AttributeMetadata.LABEL, QueryRule.Operator.FUZZY_MATCH,
				"height");
		rules.add(targetQueryRuleLabel);
		QueryRule targetQueryRuleOntologyTermTag = new QueryRule(AttributeMetadata.LABEL,
				QueryRule.Operator.FUZZY_MATCH, "standing height");
		rules.add(targetQueryRuleOntologyTermTag);
		QueryRule targetQueryRuleOntologyTermTagSyn = new QueryRule(AttributeMetadata.LABEL,
				QueryRule.Operator.FUZZY_MATCH, "length");
		rules.add(targetQueryRuleOntologyTermTagSyn);
		QueryRule disMaxQueryRule = new QueryRule(rules);
		disMaxQueryRule.setOperator(QueryRule.Operator.DIS_MAX);

		when(semanticSearchServiceHelper
				.createDisMaxQueryRuleForAttribute(Sets.newHashSet("targetAttribute"), Collections.emptyList()))
				.thenReturn(disMaxQueryRule);

		Entity entity1 = mock(Entity.class);
		when(entity1.getString(AttributeMetadata.NAME)).thenReturn("height_0");
		when(entity1.getString(AttributeMetadata.LABEL)).thenReturn("height");
		when(entity1.getString(AttributeMetadata.DESCRIPTION)).thenReturn("this is a height measurement in m!");

		List<QueryRule> disMaxQueryRules = Lists
				.newArrayList(new QueryRule(AttributeMetadata.ID, QueryRule.Operator.IN, attributeIdentifiers),
						new QueryRule(QueryRule.Operator.AND), disMaxQueryRule);

		Attribute attributeHeight = attrMetaDataFactory.create().setName("height_0");
		Attribute attributeWeight = attrMetaDataFactory.create().setName("weight_0");
		sourceEntityType.addAttribute(attributeHeight);
		sourceEntityType.addAttribute(attributeWeight);

		// Case 1
		when(dataService.findAll(ATTRIBUTE_META_DATA, new QueryImpl<>(disMaxQueryRules)))
				.thenReturn(Stream.of(entity1));

		Map<Attribute, ExplainedAttribute> termsActual1 = semanticSearchService
				.findAttributes(sourceEntityType, Sets.newHashSet("targetAttribute"), Collections.emptyList());

		Map<Attribute, ExplainedAttribute> termsExpected1 = ImmutableMap
				.of(attributeHeight, ExplainedAttribute.create(attributeHeight));

		assertEquals(termsActual1.toString(), termsExpected1.toString());

		// Case 2
		when(dataService.findAll(ATTRIBUTE_META_DATA, new QueryImpl<>(disMaxQueryRules))).thenReturn(Stream.empty());

		Map<Attribute, ExplainedAttribute> termsActual2 = semanticSearchService
				.findAttributes(sourceEntityType, Sets.newHashSet("targetAttribute"), Collections.emptyList());

		Map<Attribute, ExplainedAttribute> termsExpected2 = ImmutableMap.of();

		assertEquals(termsActual2, termsExpected2);

		Mockito.reset(ontologyService);
		attribute.setDescription("Standing height (Ångstrøm)");

		when(ontologyService.findOntologyTerms(ontologies, ImmutableSet.of("standing", "height", "ångstrøm"), 100))
				.thenReturn(ontologyTerms);
		Hit<OntologyTerm> result = semanticSearchService.findTags(attribute, ontologies);
		assertEquals(result, Hit.<OntologyTerm>create(standingHeight, 0.76471f));
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
		SemanticSearchServiceImpl semanticSearchService()
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
