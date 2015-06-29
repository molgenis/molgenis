package org.molgenis.data.semanticsearch.service.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.meta.AttributeMetaDataMetaData;
import org.molgenis.data.meta.EntityMetaDataMetaData;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.semantic.Relation;
import org.molgenis.data.semanticsearch.explain.service.ElasticSearchExplainService;
import org.molgenis.data.semanticsearch.service.OntologyTagService;
import org.molgenis.data.semanticsearch.service.SemanticSearchService;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.ontology.core.service.OntologyService;
import org.molgenis.ontology.ic.TermFrequencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

@ContextConfiguration(classes = SemanticSearchServiceHelperTest.Config.class)
public class SemanticSearchServiceHelperTest extends AbstractTestNGSpringContextTests
{
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

	@Autowired
	private MetaDataService metaDataService;

	@Test
	public void testCreateDisMaxQueryRule()
	{
		List<String> createdTargetAttributeQueries = Arrays.asList("Height", "Standing height in cm", "body_length",
				"Sitting height", "sitting_length", "Height", "sature");
		QueryRule actualRule = semanticSearchServiceHelper.createDisMaxQueryRuleForTerms(createdTargetAttributeQueries);
		String expectedQueryRuleToString = "(label FUZZY_MATCH 'Height'description FUZZY_MATCH 'Height'label FUZZY_MATCH 'Standing height in cm'description FUZZY_MATCH 'Standing height in cm'label FUZZY_MATCH 'body_length'description FUZZY_MATCH 'body_length'label FUZZY_MATCH 'Sitting height'description FUZZY_MATCH 'Sitting height'label FUZZY_MATCH 'sitting_length'description FUZZY_MATCH 'sitting_length'label FUZZY_MATCH 'Height'description FUZZY_MATCH 'Height'label FUZZY_MATCH 'sature'description FUZZY_MATCH 'sature')";
		assertEquals(actualRule.getOperator(), Operator.DIS_MAX);
		assertEquals(actualRule.toString(), expectedQueryRuleToString);

		List<String> createdTargetAttributeQueries2 = Arrays.asList("(Height) [stand^~]");
		QueryRule actualRule2 = semanticSearchServiceHelper.createDisMaxQueryRuleForTerms(createdTargetAttributeQueries2);
		String expectedQueryRuleToString2 = "(label FUZZY_MATCH '\\(Height\\) \\[stand^\\~\\]'description FUZZY_MATCH '\\(Height\\) \\[stand^\\~\\]')";
		assertEquals(actualRule2.getOperator(), Operator.DIS_MAX);
		assertEquals(actualRule2.toString(), expectedQueryRuleToString2);
	}

	@Test
	public void testCreateShouldQueryRule()
	{
		String multiOntologyTermIri = "http://www.molgenis.org/1,http://www.molgenis.org/2";
		OntologyTerm ontologyTerm_1 = OntologyTerm.create("http://www.molgenis.org/1", "molgenis label in the gcc");
		OntologyTerm ontologyTerm_2 = OntologyTerm.create("http://www.molgenis.org/2",
				"molgenis label 2 in the genetics", Arrays.asList("label 2"));
		when(ontologyService.getOntologyTerm(ontologyTerm_1.getIRI())).thenReturn(ontologyTerm_1);
		when(ontologyService.getOntologyTerm(ontologyTerm_2.getIRI())).thenReturn(ontologyTerm_2);

		QueryRule actualShouldQueryRule = semanticSearchServiceHelper.createShouldQueryRule(multiOntologyTermIri);
		String expectedShouldQueryRuleToString = "((label FUZZY_MATCH 'gcc molgenis label'description FUZZY_MATCH 'gcc molgenis label')(label FUZZY_MATCH '2 label'description FUZZY_MATCH '2 label'label FUZZY_MATCH '2 genetics molgenis label'description FUZZY_MATCH '2 genetics molgenis label'))";

		assertEquals(actualShouldQueryRule.toString(), expectedShouldQueryRuleToString);
		assertEquals(actualShouldQueryRule.getOperator(), Operator.SHOULD);
	}

	@Test
	public void testCreateTargetAttributeQueryTerms()
	{
		EntityMetaData targetEntityMetaData = new DefaultEntityMetaData("targetEntityMetaData");
		DefaultAttributeMetaData targetAttribute_1 = new DefaultAttributeMetaData("targetAttribute 1");
		targetAttribute_1.setDescription("Height");

		DefaultAttributeMetaData targetAttribute_2 = new DefaultAttributeMetaData("targetAttribute 2");
		targetAttribute_2.setLabel("Height");

		DefaultAttributeMetaData targetAttribute_3 = new DefaultAttributeMetaData("targetAttribute 3");

		Multimap<Relation, OntologyTerm> tags = LinkedHashMultimap.<Relation, OntologyTerm> create();
		OntologyTerm ontologyTerm1 = OntologyTerm.create("http://onto/standingheight", "Standing height",
				"Description is not used", Arrays.<String> asList("body_length"));
		OntologyTerm ontologyTerm2 = OntologyTerm.create("http://onto/sittingheight", "Sitting height",
				"Description is not used", Arrays.<String> asList("sitting_length"));
		OntologyTerm ontologyTerm3 = OntologyTerm.create("http://onto/height", "Height", "Description is not used",
				Arrays.<String> asList("sature"));

		tags.put(Relation.isAssociatedWith, ontologyTerm1);
		tags.put(Relation.isRealizationOf, ontologyTerm2);
		tags.put(Relation.isDefinedBy, ontologyTerm3);

		when(ontologyTagService.getTagsForAttribute(targetEntityMetaData, targetAttribute_1)).thenReturn(tags);

		when(ontologyTagService.getTagsForAttribute(targetEntityMetaData, targetAttribute_2)).thenReturn(tags);

		when(ontologyTagService.getTagsForAttribute(targetEntityMetaData, targetAttribute_3)).thenReturn(tags);

		// Case 1
		QueryRule actualTargetAttributeQueryTerms_1 = semanticSearchServiceHelper.createDisMaxQueryRuleForAttribute(
				targetEntityMetaData, targetAttribute_1);
		String expecteddisMaxQueryRuleToString_1 = "(label FUZZY_MATCH '1 targetattribute'description FUZZY_MATCH '1 targetattribute'label FUZZY_MATCH 'height'description FUZZY_MATCH 'height'label FUZZY_MATCH 'length body'description FUZZY_MATCH 'length body'label FUZZY_MATCH 'standing height'description FUZZY_MATCH 'standing height'label FUZZY_MATCH 'length sitting'description FUZZY_MATCH 'length sitting'label FUZZY_MATCH 'sitting height'description FUZZY_MATCH 'sitting height'label FUZZY_MATCH 'sature'description FUZZY_MATCH 'sature'label FUZZY_MATCH 'height'description FUZZY_MATCH 'height')";
		assertEquals(actualTargetAttributeQueryTerms_1.toString(), expecteddisMaxQueryRuleToString_1);

		// Case 2
		QueryRule expecteddisMaxQueryRuleToString_2 = semanticSearchServiceHelper.createDisMaxQueryRuleForAttribute(
				targetEntityMetaData, targetAttribute_2);
		String expectedTargetAttributeQueryTermsToString_2 = "(label FUZZY_MATCH 'height'description FUZZY_MATCH 'height'label FUZZY_MATCH 'length body'description FUZZY_MATCH 'length body'label FUZZY_MATCH 'standing height'description FUZZY_MATCH 'standing height'label FUZZY_MATCH 'length sitting'description FUZZY_MATCH 'length sitting'label FUZZY_MATCH 'sitting height'description FUZZY_MATCH 'sitting height'label FUZZY_MATCH 'sature'description FUZZY_MATCH 'sature'label FUZZY_MATCH 'height'description FUZZY_MATCH 'height')";
		assertEquals(expecteddisMaxQueryRuleToString_2.toString(), expectedTargetAttributeQueryTermsToString_2);

		// Case 3
		QueryRule expecteddisMaxQueryRuleToString_3 = semanticSearchServiceHelper.createDisMaxQueryRuleForAttribute(
				targetEntityMetaData, targetAttribute_3);
		String expectedTargetAttributeQueryTermsToString_3 = "(label FUZZY_MATCH '3 targetattribute'description FUZZY_MATCH '3 targetattribute'label FUZZY_MATCH 'length body'description FUZZY_MATCH 'length body'label FUZZY_MATCH 'standing height'description FUZZY_MATCH 'standing height'label FUZZY_MATCH 'length sitting'description FUZZY_MATCH 'length sitting'label FUZZY_MATCH 'sitting height'description FUZZY_MATCH 'sitting height'label FUZZY_MATCH 'sature'description FUZZY_MATCH 'sature'label FUZZY_MATCH 'height'description FUZZY_MATCH 'height')";
		assertEquals(expecteddisMaxQueryRuleToString_3.toString(), expectedTargetAttributeQueryTermsToString_3);
	}

	@Test
	public void testCollectQueryTermsFromOntologyTerm()
	{
		// Case 1
		OntologyTerm ontologyTerm1 = OntologyTerm.create("http://onto/standingheight", "Standing height",
				"Description is not used", Arrays.<String> asList("body_length"));
		List<String> actual_1 = semanticSearchServiceHelper.parseOntologyTermQueries(ontologyTerm1);
		assertEquals(actual_1, Arrays.asList("length body", "standing height"));

		// Case 2
		OntologyTerm ontologyTerm2 = OntologyTerm.create("http://onto/standingheight", "height",
				"Description is not used", Collections.emptyList());

		OntologyTerm ontologyTerm3 = OntologyTerm.create("http://onto/standingheight-children", "length",
				Arrays.<String> asList("body_length"));

		when(ontologyService.getChildren(ontologyTerm2)).thenReturn(Arrays.asList(ontologyTerm3));

		when(ontologyService.getOntologyTermDistance(ontologyTerm2, ontologyTerm3)).thenReturn(1);

		List<String> actual_2 = semanticSearchServiceHelper.parseOntologyTermQueries(ontologyTerm2);

		assertEquals(actual_2, Arrays.asList("height", "length^0.5 body^0.5", "length^0.5"));
	}

	@Test
	public void testGetAttributeIdentifiers()
	{
		EntityMetaData sourceEntityMetaData = new DefaultEntityMetaData("sourceEntityMetaData");
		Entity entityMetaDataEntity = mock(DefaultEntity.class);

		when(
				dataService.findOne(EntityMetaDataMetaData.ENTITY_NAME,
						new QueryImpl().eq(EntityMetaDataMetaData.FULL_NAME, sourceEntityMetaData.getName())))
				.thenReturn(entityMetaDataEntity);

		Entity attributeEntity1 = mock(DefaultEntity.class);
		when(attributeEntity1.getString(AttributeMetaDataMetaData.IDENTIFIER)).thenReturn("1");
		Entity attributeEntity2 = mock(DefaultEntity.class);
		when(attributeEntity2.getString(AttributeMetaDataMetaData.IDENTIFIER)).thenReturn("2");
		when(entityMetaDataEntity.getEntities(EntityMetaDataMetaData.ATTRIBUTES)).thenReturn(
				Arrays.<Entity> asList(attributeEntity1, attributeEntity2));

		List<String> expactedAttributeIdentifiers = Arrays.<String> asList("1", "2");
		assertEquals(semanticSearchServiceHelper.getAttributeIdentifiers(sourceEntityMetaData),
				expactedAttributeIdentifiers);
	}

	@Test
	public void testParseBoostQueryString()
	{
		String description = "falling in the ocean!";
		String actual = semanticSearchServiceHelper.parseBoostQueryString(description, 0.5);
		assertEquals(actual, "ocean^0.5 falling^0.5");
	}

	@Test
	public void testRemoveStopWords()
	{
		String description = "falling in the ocean!";
		Set<String> actual = semanticSearchServiceHelper.removeStopWords(description);
		Set<String> expected = Sets.newHashSet("falling", "ocean");
		assertEquals(actual, expected);
	}

	@Test
	public void testFindTagsSync()
	{
		String description = "Fall " + SemanticSearchServiceHelper.STOP_WORDS + " sleep";
		List<String> ontologyIds = Arrays.<String> asList("1");
		Set<String> searchTerms = Sets.newHashSet("fall", "sleep");
		semanticSearchServiceHelper.findTags(description, ontologyIds);
		verify(ontologyService).findOntologyTerms(ontologyIds, searchTerms, SemanticSearchServiceHelper.MAX_NUM_TAGS);
	}

	@Test
	public void testSearchCircumflex() throws InterruptedException, ExecutionException
	{
		String description = "body^0.5 length^0.5";
		Set<String> expected = Sets.newHashSet("length^0.5", "body^0.5");
		Set<String> actual = semanticSearchServiceHelper.removeStopWords(description);
		assertEquals(actual, expected);
	}

	@Test
	public void testSearchTilde() throws InterruptedException, ExecutionException
	{
		String description = "body~0.5 length~0.5";
		Set<String> expected = Sets.newHashSet("length~0.5", "body~0.5");
		Set<String> actual = semanticSearchServiceHelper.removeStopWords(description);
		assertEquals(actual, expected);
	}

	@Test
	public void testSearchUnderScore() throws InterruptedException, ExecutionException
	{
		String description = "body_length";
		Set<String> expected = Sets.newHashSet("body", "length");
		Set<String> actual = semanticSearchServiceHelper.removeStopWords(description);
		assertEquals(actual, expected);
	}

	@Test
	public void testSearchIsoLatin() throws InterruptedException, ExecutionException
	{
		String description = "Standing height (Ångstrøm)";
		List<String> ontologyIds = Arrays.<String> asList("1");
		Set<String> searchTerms = Sets.newHashSet("standing", "height", "ångstrøm");
		semanticSearchServiceHelper.findTags(description, ontologyIds);
		verify(ontologyService).findOntologyTerms(ontologyIds, searchTerms, SemanticSearchServiceHelper.MAX_NUM_TAGS);
	}

	@Test
	public void testSearchUnicode() throws InterruptedException, ExecutionException
	{
		String description = "/əˈnædrəməs/";
		List<String> ontologyIds = Arrays.<String> asList("1");
		Set<String> searchTerms = Sets.newHashSet("əˈnædrəməs");
		semanticSearchServiceHelper.findTags(description, ontologyIds);
		verify(ontologyService).findOntologyTerms(ontologyIds, searchTerms, SemanticSearchServiceHelper.MAX_NUM_TAGS);
	}

	@Configuration
	public static class Config
	{
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
		DataService dataService()
		{
			return mock(DataService.class);
		}

		@Bean
		MetaDataService metaDataService()
		{
			return mock(MetaDataService.class);
		}

		@Bean
		OntologyTagService ontologyTagService()
		{
			return mock(OntologyTagService.class);
		}

		@Bean
		ElasticSearchExplainService elasticSearchExplainService()
		{
			return mock(ElasticSearchExplainService.class);
		}

		@Bean
		TermFrequencyService termFrequencyService()
		{
			return mock(TermFrequencyService.class);
		}

		@Bean
		SemanticSearchServiceHelper semanticSearchServiceHelper()
		{
			return new SemanticSearchServiceHelper(ontologyTagService(), dataService(), ontologyService(),
					termFrequencyService());
		}
	}
}
