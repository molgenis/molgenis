package org.molgenis.data.semanticsearch.service.impl;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.semantic.Relation;
import org.molgenis.data.semanticsearch.service.bean.SearchParam;
import org.molgenis.data.semanticsearch.service.bean.TagGroup;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.ontology.core.service.OntologyService;
import org.molgenis.ontology.ic.TermFrequencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.collect.Sets.newLinkedHashSet;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.ontology.core.repository.OntologyTermRepository.DEFAULT_EXPANSION_LEVEL;
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = QueryExpansionServiceImplTest.Config.class)
public class QueryExpansionServiceImplTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	OntologyService ontologyService;
	@Autowired
	TermFrequencyService termFrequencyService;
	@Autowired
	QueryExpansionServiceImpl queryExpansionService;

	@Test
	public void testCreateDisMaxQueryRule()
	{
		List<String> createdTargetAttributeQueries = Arrays
				.asList("Height", "Standing height in cm", "body_length", "Sitting height", "sitting_length", "Height",
						"sature");
		QueryRule actualRule = queryExpansionService.createDisMaxQueryRuleForTerms(createdTargetAttributeQueries, null);
		String expectedQueryRuleToString = "DIS_MAX ('label' FUZZY_MATCH 'Height', 'description' FUZZY_MATCH 'Height', 'label' FUZZY_MATCH 'Standing height in cm', 'description' FUZZY_MATCH 'Standing height in cm', 'label' FUZZY_MATCH 'body_length', 'description' FUZZY_MATCH 'body_length', 'label' FUZZY_MATCH 'Sitting height', 'description' FUZZY_MATCH 'Sitting height', 'label' FUZZY_MATCH 'sitting_length', 'description' FUZZY_MATCH 'sitting_length', 'label' FUZZY_MATCH 'sature', 'description' FUZZY_MATCH 'sature')";
		assertEquals(actualRule.getOperator(), Operator.DIS_MAX);
		assertEquals(actualRule.toString(), expectedQueryRuleToString);

		List<String> createdTargetAttributeQueries2 = Arrays.asList("(Height) [stand^~]");
		QueryRule actualRule2 = queryExpansionService
				.createDisMaxQueryRuleForTerms(createdTargetAttributeQueries2, null);
		String expectedQueryRuleToString2 = "DIS_MAX ('label' FUZZY_MATCH '\\(Height\\) \\[stand^\\~\\]', 'description' FUZZY_MATCH '\\(Height\\) \\[stand^\\~\\]')";
		assertEquals(actualRule2.getOperator(), Operator.DIS_MAX);
		assertEquals(actualRule2.toString(), expectedQueryRuleToString2);
	}

	@Test
	public void testCreateQueryRulesForOntologyTerms()
	{
		OntologyTerm ontologyTerm_1 = OntologyTerm
				.create("1", "http://www.molgenis.org/1", "molgenis label in the gcc", Arrays.asList("label 2"));
		OntologyTerm ontologyTerm_2 = OntologyTerm
				.create("2", "http://www.molgenis.org/2", "molgenis label 2 in the genetics", Arrays.asList("label 2"));
		OntologyTerm ontologyTerm_3 = OntologyTerm
				.create("3", "http://www.molgenis.org/3", "molgenis child", Arrays.asList("child"));

		TagGroup hit_1 = TagGroup.create(ontologyTerm_1, "molgenis", 0.5f);
		TagGroup hit_2 = TagGroup.create(ontologyTerm_2, "molgenis", 0.5f);
		TagGroup hit_4 = TagGroup.create(Arrays.asList(ontologyTerm_1, ontologyTerm_2), "molgenis child", 1.0f);

		when(ontologyService.getChildren(ontologyTerm_1, DEFAULT_EXPANSION_LEVEL)).thenReturn(emptyList());
		when(ontologyService.getChildren(ontologyTerm_2, DEFAULT_EXPANSION_LEVEL))
				.thenReturn(Arrays.asList(ontologyTerm_3));
		when(ontologyService.getOntologyTermDistance(ontologyTerm_2, ontologyTerm_3)).thenReturn(1);

		// Case one
		QueryRule createQueryRulesForOntologyTerms1 = queryExpansionService
				.createQueryRuleForOntologyTerms(asList(hit_1, hit_2));

		String expectedShouldQueryRuleToString1 = "DIS_MAX ('label' FUZZY_MATCH 'label 2', 'description' FUZZY_MATCH 'label 2', 'label' FUZZY_MATCH 'molgenis label gcc', 'description' FUZZY_MATCH 'molgenis label gcc', 'label' FUZZY_MATCH 'molgenis label 2 genetics', 'description' FUZZY_MATCH 'molgenis label 2 genetics', 'label' FUZZY_MATCH 'child^0.5', 'description' FUZZY_MATCH 'child^0.5', 'label' FUZZY_MATCH 'molgenis^0.5 child^0.5', 'description' FUZZY_MATCH 'molgenis^0.5 child^0.5')";
		assertEquals(createQueryRulesForOntologyTerms1.toString(), expectedShouldQueryRuleToString1);

		// Case two
		QueryRule createQueryRulesForOntologyTerms2 = queryExpansionService
				.createQueryRuleForOntologyTerms(asList(hit_4));

		String expectedShouldQueryRuleToString2 = "SHOULD (DIS_MAX ('label' FUZZY_MATCH 'label 2', 'description' FUZZY_MATCH 'label 2', 'label' FUZZY_MATCH 'molgenis label gcc', 'description' FUZZY_MATCH 'molgenis label gcc'), DIS_MAX ('label' FUZZY_MATCH 'label 2', 'description' FUZZY_MATCH 'label 2', 'label' FUZZY_MATCH 'molgenis label 2 genetics', 'description' FUZZY_MATCH 'molgenis label 2 genetics', 'label' FUZZY_MATCH 'child^0.5', 'description' FUZZY_MATCH 'child^0.5', 'label' FUZZY_MATCH 'molgenis^0.5 child^0.5', 'description' FUZZY_MATCH 'molgenis^0.5 child^0.5'))";
		assertEquals(createQueryRulesForOntologyTerms2.toString(), expectedShouldQueryRuleToString2);
	}

	@Test
	public void testCreateTargetAttributeQueryTerms()
	{
		AttributeMetaData targetAttribute_1 = mock(AttributeMetaData.class);
		when(targetAttribute_1.getName()).thenReturn("targetAttribute 1");
		when(targetAttribute_1.getDescription()).thenReturn("Height");

		AttributeMetaData targetAttribute_2 = mock(AttributeMetaData.class);
		when(targetAttribute_2.getName()).thenReturn("targetAttribute 2");
		when(targetAttribute_2.getLabel()).thenReturn("Height");

		Multimap<Relation, OntologyTerm> tags = LinkedHashMultimap.create();
		OntologyTerm ontologyTerm1 = OntologyTerm
				.create("http://onto/standingheight", "Standing height", "Description is not used",
						asList("body_length"));
		OntologyTerm ontologyTerm2 = OntologyTerm
				.create("http://onto/sittingheight", "Sitting height", "Description is not used",
						asList("sitting_length"));
		OntologyTerm ontologyTerm3 = OntologyTerm
				.create("http://onto/height", "Height", "Description is not used", asList("sature"));
		OntologyTerm ontologyTerm4 = OntologyTerm
				.create("http://onto/heightsature", "Height", "Description is not used", asList("Height sature"));

		tags.put(Relation.isAssociatedWith, ontologyTerm1);
		tags.put(Relation.isRealizationOf, ontologyTerm2);
		tags.put(Relation.isDefinedBy, ontologyTerm3);
		tags.put(Relation.isDefinedBy, ontologyTerm4);

		when(ontologyService.getChildren(ontologyTerm1, DEFAULT_EXPANSION_LEVEL)).thenReturn(emptyList());
		when(ontologyService.getChildren(ontologyTerm2, DEFAULT_EXPANSION_LEVEL)).thenReturn(emptyList());
		when(ontologyService.getChildren(ontologyTerm3, DEFAULT_EXPANSION_LEVEL)).thenReturn(emptyList());
		when(ontologyService.getChildren(ontologyTerm4, DEFAULT_EXPANSION_LEVEL)).thenReturn(emptyList());

		when(termFrequencyService.getTermFrequency("targetattribute")).thenReturn(1.0f);
		when(termFrequencyService.getTermFrequency("height")).thenReturn(1.0f);
		when(termFrequencyService.getTermFrequency("1")).thenReturn(1.0f);
		when(termFrequencyService.getTermFrequency("2")).thenReturn(0.5f);
		when(termFrequencyService.getTermFrequency("3")).thenReturn(0.2f);

		List<TagGroup> ontologyTermHits = tags.values().stream().map(ot -> TagGroup.create(ot, ot.getLabel(), 1.0f))
				.collect(Collectors.toList());

		// Case 1
		SearchParam searchParam_1 = SearchParam
				.create(newLinkedHashSet(asList("targetAttribute 1", "Height")), ontologyTermHits);
		QueryRule actualTargetAttributeQueryTerms_1 = queryExpansionService.expand(searchParam_1);
		String expecteddisMaxQueryRuleToString_1 = "DIS_MAX (DIS_MAX ('label' FUZZY_MATCH 'targetattribute^1.0 1^1.0', 'description' FUZZY_MATCH 'targetattribute^1.0 1^1.0', 'label' FUZZY_MATCH 'height^1.0', 'description' FUZZY_MATCH 'height^1.0'), DIS_MAX ('label' FUZZY_MATCH 'body length', 'description' FUZZY_MATCH 'body length', 'label' FUZZY_MATCH 'description used', 'description' FUZZY_MATCH 'description used', 'label' FUZZY_MATCH 'sitting length', 'description' FUZZY_MATCH 'sitting length', 'label' FUZZY_MATCH 'sature', 'description' FUZZY_MATCH 'sature', 'label' FUZZY_MATCH 'height sature', 'description' FUZZY_MATCH 'height sature'))";
		assertEquals(actualTargetAttributeQueryTerms_1.toString(), expecteddisMaxQueryRuleToString_1);

		// Case 2
		SearchParam searchParam_2 = SearchParam.create(Sets.newHashSet("Height"), ontologyTermHits);
		QueryRule expecteddisMaxQueryRuleToString_2 = queryExpansionService.expand(searchParam_2);
		String expectedTargetAttributeQueryTermsToString_2 = "DIS_MAX (DIS_MAX ('label' FUZZY_MATCH 'height^1.0', 'description' FUZZY_MATCH 'height^1.0'), DIS_MAX ('label' FUZZY_MATCH 'body length', 'description' FUZZY_MATCH 'body length', 'label' FUZZY_MATCH 'description used', 'description' FUZZY_MATCH 'description used', 'label' FUZZY_MATCH 'sitting length', 'description' FUZZY_MATCH 'sitting length', 'label' FUZZY_MATCH 'sature', 'description' FUZZY_MATCH 'sature', 'label' FUZZY_MATCH 'height sature', 'description' FUZZY_MATCH 'height sature'))";
		assertEquals(expecteddisMaxQueryRuleToString_2.toString(), expectedTargetAttributeQueryTermsToString_2);

		// Case 3
		SearchParam searchParam_3 = SearchParam.create(Sets.newHashSet("targetAttribute"), ontologyTermHits);
		QueryRule expecteddisMaxQueryRuleToString_3 = queryExpansionService.expand(searchParam_3);
		String expectedTargetAttributeQueryTermsToString_3 = "DIS_MAX (DIS_MAX ('label' FUZZY_MATCH 'targetattribute^1.0', 'description' FUZZY_MATCH 'targetattribute^1.0'), DIS_MAX ('label' FUZZY_MATCH 'body length', 'description' FUZZY_MATCH 'body length', 'label' FUZZY_MATCH 'description used', 'description' FUZZY_MATCH 'description used', 'label' FUZZY_MATCH 'sitting length', 'description' FUZZY_MATCH 'sitting length', 'label' FUZZY_MATCH 'sature', 'description' FUZZY_MATCH 'sature', 'label' FUZZY_MATCH 'height sature', 'description' FUZZY_MATCH 'height sature'))";
		assertEquals(expecteddisMaxQueryRuleToString_3.toString(), expectedTargetAttributeQueryTermsToString_3);
	}

	@Test
	public void testCollectQueryTermsFromOntologyTerm()
	{
		// Case 1
		OntologyTerm ontologyTerm1 = OntologyTerm
				.create("1", "http://onto/standingheight", "Standing height", Arrays.asList("body_length"));
		when(ontologyService.getChildren(ontologyTerm1, DEFAULT_EXPANSION_LEVEL)).thenReturn(emptyList());

		List<String> actual_1 = queryExpansionService.getExpandedQueriesFromOntologyTerm(ontologyTerm1);
		assertEquals(actual_1, Arrays.asList("body length", "standing height"));

		// Case 2
		OntologyTerm ontologyTerm2 = OntologyTerm.create("2", "http://onto/standingheight", "height", emptyList());

		OntologyTerm ontologyTerm3 = OntologyTerm
				.create("3", "http://onto/standingheight-children", "length", Arrays.<String>asList("body_length"));

		when(ontologyService.getChildren(ontologyTerm2, DEFAULT_EXPANSION_LEVEL))
				.thenReturn(Lists.newArrayList(ontologyTerm3));

		when(ontologyService.getOntologyTermDistance(ontologyTerm2, ontologyTerm3)).thenReturn(1);

		List<String> actual_2 = queryExpansionService.getExpandedQueriesFromOntologyTerm(ontologyTerm2);

		assertEquals(actual_2, Arrays.asList("height", "body^0.5 length^0.5", "length^0.5"));
	}

	@Test
	public void testParseBoostQueryString()
	{
		String description = "falling in the ocean!";
		String actual = queryExpansionService.parseBoostQueryString(description, 0.5);
		assertEquals(actual, "falling^0.5 ocean^0.5");
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
		TermFrequencyService termFrequencyService()
		{
			return mock(TermFrequencyService.class);
		}

		@Bean
		QueryExpansionServiceImpl QueryExpansionService()
		{
			return new QueryExpansionServiceImpl(ontologyService(), termFrequencyService());
		}
	}
}