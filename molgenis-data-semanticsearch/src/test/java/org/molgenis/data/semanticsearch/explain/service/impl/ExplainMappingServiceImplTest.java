package org.molgenis.data.semanticsearch.explain.service.impl;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.semanticsearch.explain.service.impl.ExplainMappingServiceImpl.STRICT_MATCHING_CRITERION;
import static org.molgenis.ontology.core.repository.OntologyTermRepository.DEFAULT_EXPANSION_LEVEL;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.elasticsearch.common.collect.Lists;
import org.molgenis.data.semanticsearch.explain.bean.ExplainedMatchCandidate;
import org.molgenis.data.semanticsearch.explain.bean.ExplainedQueryString;
import org.molgenis.data.semanticsearch.explain.bean.OntologyTermQueryExpansionSolution;
import org.molgenis.data.semanticsearch.explain.service.ExplainMappingService;
import org.molgenis.data.semanticsearch.semantic.Hit;
import org.molgenis.data.semanticsearch.service.TagGroupGenerator;
import org.molgenis.data.semanticsearch.service.bean.SearchParam;
import org.molgenis.data.semanticsearch.service.bean.TagGroup;
import org.molgenis.data.semanticsearch.utils.SemanticSearchServiceUtils;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.ontology.core.service.OntologyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import junit.framework.Assert;

@WebAppConfiguration
@ContextConfiguration(classes = ExplainMappingServiceImplTest.Config.class)
public class ExplainMappingServiceImplTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	OntologyService ontologyService;

	@Autowired
	TagGroupGenerator tagGroupGenerator;

	@Autowired
	ExplainMappingService explainMappingService;

	@Test
	public void testExplainMapping()
	{
		String heightCandidate = "Height in cm";
		String weightCandidate = "Weight in kg";

		Set<String> heightCandidateWords = SemanticSearchServiceUtils.splitRemoveStopWords(heightCandidate);
		Set<String> weightCandidateWords = SemanticSearchServiceUtils.splitRemoveStopWords(weightCandidate);

		OntologyTerm height = OntologyTerm.create("1", "iri1", "Height",
				Arrays.asList("Height", "Body Length"));
		OntologyTerm weight = OntologyTerm.create("2", "iri2", "Weight", Arrays.asList("Weight"));

		TagGroup targetTagGroup = TagGroup.create(Arrays.asList(height, weight), "Height Weight", 1.0f);
		TagGroup heightTagGroup = TagGroup.create(Arrays.asList(height), "Heigh", 0.8f);
		TagGroup weightTagGroup = TagGroup.create(Arrays.asList(weight), "Weight", 0.8f);

		SearchParam searchParam = SearchParam.create(Sets.newHashSet("Body Mass Index"), Arrays.asList(targetTagGroup));

		when(ontologyService.getAllOntologiesIds()).thenReturn(Arrays.asList("1"));
		when(ontologyService.getOntologyTerms(targetTagGroup.getCombinedOntologyTerm().getAtomicIRIs()))
				.thenReturn(Arrays.asList(height, weight));

		when(ontologyService.getChildren(height, DEFAULT_EXPANSION_LEVEL)).thenReturn(Collections.emptyList());
		when(ontologyService.getChildren(weight, DEFAULT_EXPANSION_LEVEL)).thenReturn(Collections.emptyList());

		when(ontologyService.findOntologyTerms(Arrays.asList("1"), heightCandidateWords, 2,
				Arrays.asList(height, weight))).thenReturn(Arrays.asList(height));
		when(ontologyService.findOntologyTerms(Arrays.asList("1"), weightCandidateWords, 2,
				Arrays.asList(height, weight))).thenReturn(Arrays.asList(weight));

		when(tagGroupGenerator.applyTagMatchingCriterion(Arrays.asList(height), heightCandidateWords,
				STRICT_MATCHING_CRITERION)).thenReturn(asList(heightTagGroup));
		when(tagGroupGenerator.applyTagMatchingCriterion(Arrays.asList(weight), weightCandidateWords,
				STRICT_MATCHING_CRITERION)).thenReturn(asList(weightTagGroup));

		when(tagGroupGenerator.combineTagGroups(heightCandidateWords, asList(heightTagGroup)))
				.thenReturn(asList(heightTagGroup));
		when(tagGroupGenerator.combineTagGroups(weightCandidateWords, asList(weightTagGroup)))
				.thenReturn(asList(weightTagGroup));

		// Test one
		ExplainedMatchCandidate<String> actualExplainMappingHeight = explainMappingService.explainMapping(searchParam,
				heightCandidate);
		ExplainedMatchCandidate<String> expectedExplainMappingHeight = ExplainedMatchCandidate.create(heightCandidate,
				Arrays.asList(ExplainedQueryString.create("height", "height", "Height", 82.4f)), false);

		Assert.assertEquals(expectedExplainMappingHeight, actualExplainMappingHeight);

		// Test two
		ExplainedMatchCandidate<String> actualExplainMappingWeight = explainMappingService.explainMapping(searchParam,
				weightCandidate);
		ExplainedMatchCandidate<String> expectedExplainMappingWeight = ExplainedMatchCandidate.create(weightCandidate,
				Arrays.asList(ExplainedQueryString.create("weight", "weight", "Weight", 82.4f)), false);

		Assert.assertEquals(expectedExplainMappingWeight, actualExplainMappingWeight);
	}

	@Test
	public void testHypertension()
	{
		String target = "have you ever had hypertension";
		String source = "high blood pressure";

		OntologyTerm hypertension = OntologyTerm.create("1", "iri1", "hypertension",
				Arrays.asList("high blood pressure"));

		OntologyTermQueryExpansionSolution queryExpansionSolution = OntologyTermQueryExpansionSolution
				.create(ImmutableMap.of(hypertension, hypertension), true);

		Hit<ExplainedMatchCandidate<String>> computeScoreForMatchedSource = ((ExplainMappingServiceImpl) explainMappingService)
				.computeScoreForMatchedSource(queryExpansionSolution, target, source);

		ExplainedQueryString explanation1 = ExplainedQueryString.create("high blood pressure", "high blood pressure",
				"hypertension", 100.0f);

		Hit<ExplainedMatchCandidate<String>> expected = Hit
				.create(ExplainedMatchCandidate.create(source, Lists.newArrayList(explanation1), true), 100.0f);

		Assert.assertEquals(expected, computeScoreForMatchedSource);
	}

	@Test
	public void testHypertensionMedication()
	{
		String target = "have you ever taken any medication for hypertension?";
		String source = "have you ever taken any drugs for high blood pressure";

		OntologyTerm hypertension = OntologyTerm.create("1", "iri1", "hypertension",
				Arrays.asList("high blood pressure"));

		OntologyTerm medication = OntologyTerm.create("2", "iri2", "medication", Arrays.asList("drugs"));

		OntologyTermQueryExpansionSolution queryExpansionSolution1 = OntologyTermQueryExpansionSolution
				.create(ImmutableMap.of(hypertension, hypertension, medication, medication), true);

		Hit<ExplainedMatchCandidate<String>> computeScoreForMatchedSource = ((ExplainMappingServiceImpl) explainMappingService)
				.computeScoreForMatchedSource(queryExpansionSolution1, target, source);

		ExplainedQueryString explanation2 = ExplainedQueryString.create("high blood pressure", "high blood pressure",
				"hypertension", 77.6f);

		ExplainedQueryString explanation3 = ExplainedQueryString.create("drugs", "drugs", "medication", 28.6f);

		List<ExplainedQueryString> newArrayList = Lists.newArrayList(explanation2, explanation3);

		Hit<ExplainedMatchCandidate<String>> expected = Hit
				.create(ExplainedMatchCandidate.create(source, newArrayList, true), 88.9f);

		Assert.assertEquals(expected, computeScoreForMatchedSource);
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
		TagGroupGenerator tagGroupGenerator()
		{
			return mock(TagGroupGenerator.class);
		}

		@Bean
		ExplainMappingService explainMappingService()
		{
			return new ExplainMappingServiceImpl(ontologyService(), tagGroupGenerator());
		}
	}
}
