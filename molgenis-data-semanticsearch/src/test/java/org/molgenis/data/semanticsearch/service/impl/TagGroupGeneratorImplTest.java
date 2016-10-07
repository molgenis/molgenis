package org.molgenis.data.semanticsearch.service.impl;

import com.google.common.collect.*;
import org.mockito.Mockito;
import org.molgenis.data.semanticsearch.explain.bean.OntologyTermHit;
import org.molgenis.data.semanticsearch.service.bean.TagGroup;
import org.molgenis.data.semanticsearch.utils.SemanticSearchServiceUtils;
import org.molgenis.ontology.core.model.CombinedOntologyTerm;
import org.molgenis.ontology.core.model.OntologyTagObject;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.ontology.core.service.OntologyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static com.google.common.collect.ImmutableSet.of;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.semanticsearch.service.impl.TagGroupGeneratorImpl.MAX_NUM_TAGS;
import static org.molgenis.data.semanticsearch.service.impl.TagGroupGeneratorImpl.STRICT_MATCHING_CRITERION;
import static org.molgenis.ontology.core.model.CombinedOntologyTerm.and;
import static org.molgenis.ontology.utils.Stemmer.splitAndStem;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@ContextConfiguration(classes = TagGroupGeneratorImplTest.Config.class)
public class TagGroupGeneratorImplTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	OntologyService ontologyService;

	@Autowired
	TagGroupGeneratorImpl tagGroupGenerator;

	private OntologyTerm standingHeight;
	private OntologyTerm bodyWeight;
	private List<OntologyTerm> ontologyTerms;
	private List<String> ontologyIds;

	@BeforeMethod
	public void init()
	{
		standingHeight = OntologyTerm
				.create("id-height", "http://onto/height", "Standing height", asList("Standing height", "length"));
		bodyWeight = OntologyTerm
				.create("id-bmi", "http://onto/bmi", "Body weight", asList("Body weight", "Mass in kilograms"));
		ontologyTerms = asList(standingHeight, bodyWeight);
		ontologyIds = asList("1");
	}

	@Test
	public void testRemoveStopWords()
	{
		String description = "falling in the ocean!";
		Set<String> actual = SemanticSearchServiceUtils.splitRemoveStopWords(description);
		Set<String> expected = Sets.newHashSet("falling", "ocean");
		assertEquals(actual, expected);
	}

	@Test
	public void testSearchCircumflex() throws InterruptedException, ExecutionException
	{
		String description = "body^0.5 length^0.5";
		Set<String> expected = Sets.newHashSet("length", "body", "0", "5");
		Set<String> actual = SemanticSearchServiceUtils.splitRemoveStopWords(description);
		assertEquals(actual.size(), 4);
		assertTrue(actual.containsAll(expected));
	}

	@Test
	public void testSearchTilde() throws InterruptedException, ExecutionException
	{
		String description = "body~0.5 length~0.5";
		Set<String> expected = Sets.newLinkedHashSet(Sets.newHashSet("body", "0", "5", "length"));
		Set<String> actual = SemanticSearchServiceUtils.splitRemoveStopWords(description);
		assertEquals(actual, expected);
	}

	@Test
	public void testSearchUnderScore() throws InterruptedException, ExecutionException
	{
		String description = "body_length";
		Set<String> expected = Sets.newHashSet("body", "length");
		Set<String> actual = SemanticSearchServiceUtils.splitRemoveStopWords(description);
		assertEquals(actual, expected);
	}

	@Test
	public void testSearchHypertension() throws InterruptedException, ExecutionException
	{
		Mockito.reset(ontologyService);

		List<String> ontologyIds = asList("1");
		OntologyTerm standingHeight = OntologyTerm
				.create("id-height", "http://onto/height", "Standing height", asList("Standing height", "length"));
		OntologyTerm bodyWeight = OntologyTerm
				.create("id-bmi", "http://onto/bmi", "Body weight", asList("Body weight", "Mass in kilograms"));
		OntologyTerm hypertension = OntologyTerm.create("id-hyp", "http://onto/hyp", "Hypertension");
		OntologyTerm maternalHypertension = OntologyTerm.create("id-mhyp", "http://onto/mhyp", "Maternal hypertension");

		List<OntologyTerm> ontologyTerms = asList(standingHeight, bodyWeight, hypertension, maternalHypertension);
		Set<String> searchTerms = Sets.newLinkedHashSet(asList("history", "hypertension"));

		when(ontologyService.findOntologyTerms(ontologyIds, searchTerms, MAX_NUM_TAGS)).thenReturn(ontologyTerms);

		List<TagGroup> result = tagGroupGenerator.generateTagGroups("history hypertension", ontologyIds);

		assertEquals(result, asList(TagGroup.create(hypertension, "hypertens", 0.69231f)));
	}

	@Test
	public void testSearchDescription() throws InterruptedException, ExecutionException
	{
		Mockito.reset(ontologyService);

		List<String> ontologyIds = asList("1");
		OntologyTerm standingHeight = OntologyTerm
				.create("id-height", "http://onto/height", "Standing height", asList("Standing height", "length"));
		OntologyTerm bodyWeight = OntologyTerm
				.create("id-bmi", "http://onto/bmi", "Body weight", asList("Body weight", "Mass in kilograms"));
		OntologyTerm hypertension = OntologyTerm.create("id-hyp", "http://onto/hyp", "Hypertension");
		OntologyTerm maternalHypertension = OntologyTerm.create("id-mhyp", "http://onto/mhyp", "Maternal hypertension");

		List<OntologyTerm> ontologyTerms = asList(standingHeight, bodyWeight, hypertension, maternalHypertension);
		Set<String> searchTerms = Sets.newLinkedHashSet(asList("standing", "height", "meters"));

		when(ontologyService.findOntologyTerms(ontologyIds, searchTerms, MAX_NUM_TAGS)).thenReturn(ontologyTerms);

		List<TagGroup> result = tagGroupGenerator.generateTagGroups("standing height meters", ontologyIds);

		assertEquals(result, asList(TagGroup.create(standingHeight, "stand height", 0.81250f)));
	}

	@Test
	public void testCreateOntologyTermPairwiseCombination()
	{
		OntologyTerm ot1 = OntologyTerm.create("1", "iri1", "septin 4", asList("SEPT4"));
		OntologyTerm ot2 = OntologyTerm.create("2", "iri2", "4th of September", asList("SEPT4"));
		OntologyTerm ot3 = OntologyTerm.create("3", "iri3", "National Security Agency", asList("NSA"));
		OntologyTerm ot4 = OntologyTerm.create("4", "iri4", "National Security Advisor", asList("NSA"));
		OntologyTerm ot5 = OntologyTerm.create("5", "iri5", "National Security Area", asList("NSA"));
		OntologyTerm ot6 = OntologyTerm.create("6", "iri6", "Activity", asList("ACT"));

		Multimap<String, OntologyTermHit> multiMap = LinkedListMultimap.create();

		multiMap.putAll("SEPT4",
				asList(OntologyTermHit.create(ot1, "SEPT4", 1.0f), OntologyTermHit.create(ot2, "SEPT4", 1.0f)));

		multiMap.putAll("NSA",
				asList(OntologyTermHit.create(ot3, "NSA", 1.0f), OntologyTermHit.create(ot4, "NSA", 1.0f),
						OntologyTermHit.create(ot5, "NSA", 1.0f)));

		multiMap.putAll("ACT", asList(OntologyTermHit.create(ot6, "ACT", 1.0f)));

		List<CombinedOntologyTerm> actual = tagGroupGenerator.createTagGroups(multiMap).stream()
				.map(list -> CombinedOntologyTerm.and(list.stream().toArray(OntologyTerm[]::new))).collect(toList());

		List<OntologyTagObject> expected = asList(and(ot1, ot3, ot6), and(ot1, ot4, ot6), and(ot1, ot5, ot6),
				and(ot2, ot3, ot6), and(ot2, ot4, ot6), and(ot2, ot5, ot6));

		Comparator<OntologyTagObject> comparator = new Comparator<OntologyTagObject>()
		{
			public int compare(OntologyTagObject o1, OntologyTagObject o2)
			{
				return o1.getIRI().compareTo(o2.getIRI());
			}
		};

		Collections.sort(actual, comparator);

		Collections.sort(expected, comparator);

		assertEquals(actual, expected);
	}

	@Test
	public void testCombineOntologyTerms()
	{
		OntologyTerm ot = OntologyTerm.create("02", "iri02", "weight", asList("measured weight"));
		OntologyTerm ot0 = OntologyTerm.create("01", "iri01", "height", asList("standing height", "body length"));
		OntologyTerm ot1 = OntologyTerm.create("1", "iri1", "septin 4", asList("SEPT4"));
		OntologyTerm ot2 = OntologyTerm.create("2", "iri2", "4th of September", asList("SEPT4"));
		OntologyTerm ot3 = OntologyTerm.create("3", "iri3", "National Security Agency", asList("NSA"));
		OntologyTerm ot4 = OntologyTerm.create("4", "iri4", "National Security Advisor", asList("NSA"));
		OntologyTerm ot5 = OntologyTerm.create("5", "iri5", "National Security Area", asList("NSA"));
		OntologyTerm ot6 = OntologyTerm.create("6", "iri6", "Movement", asList("Moved"));
		OntologyTerm ot7 = OntologyTerm.create("7", "iri7", "NSA movement SEPT4");

		Set<String> searchTerms = splitAndStem("NSA has a movement on SEPT4");

		List<OntologyTerm> relevantOntologyTerms = Lists.newArrayList(ot, ot0, ot1, ot2, ot3, ot4, ot5, ot6, ot7);

		// Randomize the order of the ontology terms
		Collections.shuffle(relevantOntologyTerms);

		List<OntologyTermHit> ontologyTermHits = tagGroupGenerator
				.applyTagMatchingCriterion(relevantOntologyTerms, searchTerms, STRICT_MATCHING_CRITERION);

		List<TagGroup> combineTagGroups = tagGroupGenerator.combineTagGroups(searchTerms, ontologyTermHits);

		List<OntologyTagObject> actual = combineTagGroups.stream().map(TagGroup::getCombinedOntologyTerm)
				.collect(toList());

		List<OntologyTagObject> expected = Lists
				.newArrayList(and(ot7), and(ot6, ot1, ot4), and(ot6, ot2, ot4), and(ot6, ot1, ot3), and(ot6, ot2, ot3),
						and(ot6, ot1, ot5), and(ot6, ot2, ot5));

		assertTrue(combineTagGroups.stream().allMatch(hit -> hit.getScore() == 0.92683f));

		Comparator<OntologyTagObject> comparator = new Comparator<OntologyTagObject>()
		{
			public int compare(OntologyTagObject o1, OntologyTagObject o2)
			{
				return o1.getIRI().compareTo(o2.getIRI());
			}
		};

		Collections.sort(actual, comparator);

		Collections.sort(expected, comparator);

		assertEquals(actual, expected);
	}

	@Test
	public void testDistanceFrom()
	{
		assertEquals(tagGroupGenerator.distanceFrom("Hypertension", of("history", "hypertens")), .6923, 0.0001,
				"String distance should be equal");
		assertEquals(tagGroupGenerator.distanceFrom("Maternal Hypertension", of("history", "hypertens")), .5454, 0.0001,
				"String distance should be equal");
	}

	@Test
	public void testSearchUnicode2()
	{
		Mockito.reset(ontologyService);
		String label = "Standing height (Ångstrøm)";

		when(ontologyService.findOntologyTerms(ontologyIds, newLinkedHashSet(asList("standing", "height", "ångstrøm")),
				MAX_NUM_TAGS)).thenReturn(ontologyTerms);

		List<TagGroup> result = tagGroupGenerator.generateTagGroups(label, ontologyIds);

		assertEquals(result.size(), 1);
		assertEquals(result.get(0), TagGroup.create(standingHeight, "stand height", 0.76471f));
	}

	@Test
	public void testSearchUnicode() throws InterruptedException, ExecutionException
	{
		Mockito.reset(ontologyService);

		String label = "/əˈnædrəməs/";

		when(ontologyService.findOntologyTerms(ontologyIds, ImmutableSet.of("əˈnædrəməs"), MAX_NUM_TAGS))
				.thenReturn(ontologyTerms);

		List<TagGroup> result = tagGroupGenerator.generateTagGroups(label, ontologyIds);

		assertEquals(result, Collections.emptyList());
	}

	@Test
	public void testSearchMultipleTags() throws InterruptedException, ExecutionException
	{
		Mockito.reset(ontologyService);

		String label = "Body mass index";

		when(ontologyService
				.findOntologyTerms(ontologyIds, newLinkedHashSet(asList("body", "mass", "index")), MAX_NUM_TAGS))
				.thenReturn(ontologyTerms);

		List<TagGroup> result = tagGroupGenerator.generateTagGroups(label, ontologyIds);

		assertEquals(result, Collections.emptyList());
	}

	@Test
	public void testSearchLabel() throws InterruptedException, ExecutionException
	{
		Mockito.reset(ontologyService);
		String label = "Standing height (m.)";

		when(ontologyService
				.findOntologyTerms(ontologyIds, newLinkedHashSet(asList("standing", "height", "m.")), MAX_NUM_TAGS))
				.thenReturn(ontologyTerms);

		List<TagGroup> result = tagGroupGenerator.generateTagGroups(label, ontologyIds);

		TagGroup ontologyTermHit = TagGroup.create(standingHeight, "stand height", 0.92857f);

		assertEquals(result, asList(ontologyTermHit));
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
		TagGroupGeneratorImpl tagGroupGenerator()
		{
			return new TagGroupGeneratorImpl(ontologyService());
		}
	}
}
