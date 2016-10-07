package org.molgenis.data.semanticsearch.explain.service.impl;

import com.google.common.collect.ImmutableMap;
import org.elasticsearch.common.collect.Lists;
import org.molgenis.data.semanticsearch.explain.bean.ExplainedMatchCandidate;
import org.molgenis.data.semanticsearch.explain.bean.ExplainedQueryString;
import org.molgenis.data.semanticsearch.explain.bean.OntologyTermHit;
import org.molgenis.data.semanticsearch.explain.bean.OntologyTermQueryExpansionSolution;
import org.molgenis.data.semanticsearch.explain.service.ExplainMappingService;
import org.molgenis.data.semanticsearch.semantic.Hit;
import org.molgenis.data.semanticsearch.service.TagGroupGenerator;
import org.molgenis.data.semanticsearch.service.bean.SearchParam;
import org.molgenis.data.semanticsearch.service.bean.TagGroup;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.ontology.core.service.OntologyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.semanticsearch.explain.service.impl.ExplainMappingServiceImpl.STRICT_MATCHING_CRITERION;
import static org.molgenis.ontology.core.repository.OntologyTermRepository.DEFAULT_EXPANSION_LEVEL;

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

		Set<String> heightCandidateWords = newHashSet("height", "cm");
		Set<String> weightCandidateWords = newHashSet("weight", "kg");

		OntologyTerm heightOntologyTerm = OntologyTerm.create("1", "iri1", "Height", asList("Height", "Body Length"));
		OntologyTerm weightOntologyTerm = OntologyTerm.create("2", "iri2", "Weight", singletonList("Weight"));

		OntologyTermHit heightOntologyTermHit = OntologyTermHit.create(heightOntologyTerm, "Height", 0.8f);
		OntologyTermHit weightOntologyTermHit = OntologyTermHit.create(weightOntologyTerm, "Weight", 0.8f);

		TagGroup heightTagGroup = TagGroup.create(heightOntologyTerm, "Height", 0.8f);
		TagGroup weightTagGroup = TagGroup.create(weightOntologyTerm, "Weight", 0.8f);
		TagGroup targetTagGroup = TagGroup
				.create(asList(heightOntologyTerm, weightOntologyTerm), "Height Weight", 1.0f);

		SearchParam searchParam = SearchParam.create(singleton("Body Mass Index"), singletonList(targetTagGroup));

		when(ontologyService.getAllOntologyIds()).thenReturn(singletonList("1"));
		when(ontologyService.getOntologyTerms(asList("iri1", "iri2")))
				.thenReturn(asList(heightOntologyTerm, weightOntologyTerm));

		//TODO: Add a test that actually uses a match to a child ontology term
		when(ontologyService.getChildren(heightOntologyTerm, DEFAULT_EXPANSION_LEVEL))
				.thenReturn(Collections.emptyList());

		when(ontologyService.getChildren(weightOntologyTerm, DEFAULT_EXPANSION_LEVEL))
				.thenReturn(Collections.emptyList());

		when(ontologyService.findOntologyTerms(singletonList("1"), heightCandidateWords, 2,
				newHashSet(heightOntologyTerm, weightOntologyTerm))).thenReturn(singletonList(heightOntologyTerm));
		when(ontologyService.findOntologyTerms(singletonList("1"), weightCandidateWords, 2,
				newHashSet(heightOntologyTerm, weightOntologyTerm))).thenReturn(singletonList(weightOntologyTerm));

		when(tagGroupGenerator.applyTagMatchingCriterion(singletonList(heightOntologyTerm), heightCandidateWords,
				STRICT_MATCHING_CRITERION)).thenReturn(singletonList(heightOntologyTermHit));
		when(tagGroupGenerator.applyTagMatchingCriterion(singletonList(weightOntologyTerm), weightCandidateWords,
				STRICT_MATCHING_CRITERION)).thenReturn(singletonList(weightOntologyTermHit));

		when(tagGroupGenerator.combineTagGroups(heightCandidateWords, singletonList(heightOntologyTermHit)))
				.thenReturn(singletonList(heightTagGroup));
		when(tagGroupGenerator.combineTagGroups(weightCandidateWords, singletonList(weightOntologyTermHit)))
				.thenReturn(singletonList(weightTagGroup));

		// Test one
		ExplainedMatchCandidate<String> actualExplainMappingHeight = explainMappingService
				.explainMapping(searchParam, heightCandidate);
		ExplainedMatchCandidate<String> expectedExplainMappingHeight = ExplainedMatchCandidate.create(heightCandidate,
				singletonList(ExplainedQueryString.create("height", "height", "Height", 82.4f)), false);

		assertEquals(expectedExplainMappingHeight, actualExplainMappingHeight);

		// Test two
		ExplainedMatchCandidate<String> actualExplainMappingWeight = explainMappingService
				.explainMapping(searchParam, weightCandidate);
		ExplainedMatchCandidate<String> expectedExplainMappingWeight = ExplainedMatchCandidate.create(weightCandidate,
				singletonList(ExplainedQueryString.create("weight", "weight", "Weight", 82.4f)), false);

		assertEquals(expectedExplainMappingWeight, actualExplainMappingWeight);
	}

	@Test
	public void testHypertension()
	{
		String target = "have you ever had hypertension";
		String source = "high blood pressure";

		OntologyTerm hypertension = OntologyTerm
				.create("1", "iri1", "hypertension", singletonList("high blood pressure"));

		OntologyTermQueryExpansionSolution queryExpansionSolution = OntologyTermQueryExpansionSolution
				.create(ImmutableMap.of(hypertension, hypertension), true);

		Hit<ExplainedMatchCandidate<String>> computeScoreForMatchedSource = ((ExplainMappingServiceImpl) explainMappingService)
				.computeScoreForMatchedSource(queryExpansionSolution, target, source);

		ExplainedQueryString explanation1 = ExplainedQueryString
				.create("high blood pressure", "high blood pressure", "hypertension", 100.0f);

		Hit<ExplainedMatchCandidate<String>> expected = Hit
				.create(ExplainedMatchCandidate.create(source, Lists.newArrayList(explanation1), true), 100.0f);

		assertEquals(expected, computeScoreForMatchedSource);
	}

	@Test
	public void testHypertensionMedication()
	{
		String target = "have you ever taken any medication for hypertension?";
		String source = "have you ever taken any drugs for high blood pressure";

		OntologyTerm hypertensionOntologyTerm = OntologyTerm
				.create("1", "iri1", "hypertension", singletonList("high blood pressure"));
		OntologyTerm medicationOntologyTerm = OntologyTerm.create("2", "iri2", "medication", singletonList("drugs"));

		OntologyTermQueryExpansionSolution queryExpansionSolution1 = OntologyTermQueryExpansionSolution
				.create(ImmutableMap.of(hypertensionOntologyTerm, hypertensionOntologyTerm, medicationOntologyTerm,
						medicationOntologyTerm), true);

		Hit<ExplainedMatchCandidate<String>> computeScoreForMatchedSource = ((ExplainMappingServiceImpl) explainMappingService)
				.computeScoreForMatchedSource(queryExpansionSolution1, target, source);

		ExplainedQueryString explanation1 = ExplainedQueryString
				.create("high blood pressure", "high blood pressure", "hypertension", 77.6f);

		ExplainedQueryString explanation2 = ExplainedQueryString.create("drugs", "drugs", "medication", 28.6f);

		List<ExplainedQueryString> explanations = asList(explanation1, explanation2);

		Hit<ExplainedMatchCandidate<String>> expected = Hit
				.create(ExplainedMatchCandidate.create(source, explanations, true), 88.9f);

		assertEquals(expected, computeScoreForMatchedSource);
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
