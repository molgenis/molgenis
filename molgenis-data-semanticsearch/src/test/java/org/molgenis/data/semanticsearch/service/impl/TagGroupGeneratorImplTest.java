package org.molgenis.data.semanticsearch.service.impl;

import static com.google.common.collect.ImmutableSet.of;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.semanticsearch.service.impl.SemanticSearchServiceImpl.MAX_NUMBER_ATTRIBTUES;
import static org.molgenis.data.semanticsearch.service.impl.TagGroupGeneratorImpl.STRICT_MATCHING_CRITERION;
import static org.molgenis.ontology.core.model.OntologyTerm.and;
import static org.molgenis.ontology.utils.Stemmer.splitAndStem;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.mockito.Mockito;
import org.molgenis.data.semanticsearch.service.OntologyTagService;
import org.molgenis.data.semanticsearch.service.bean.TagGroup;
import org.molgenis.data.semanticsearch.utils.SemanticSearchServiceUtils;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.ontology.core.service.OntologyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

@ContextConfiguration(classes = TagGroupGeneratorImplTest.Config.class)
public class TagGroupGeneratorImplTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	OntologyService ontologyService;

	@Autowired
	OntologyTagService ontologyTagService;

	@Autowired
	TagGroupGeneratorImpl tagGroupGenerator;

	private DefaultAttributeMetaData attribute;
	private OntologyTerm standingHeight;
	private OntologyTerm bodyWeight;
	private List<OntologyTerm> ontologyTerms;
	private List<String> ontologyIds;

	@BeforeMethod
	public void init()
	{
		attribute = new DefaultAttributeMetaData("attr1").setLabel("attribute 1");
		standingHeight = OntologyTerm.create("id-height", "http://onto/height", "Standing height",
				Arrays.asList("Standing height", "length"));
		bodyWeight = OntologyTerm.create("id-bmi", "http://onto/bmi", "Body weight",
				Arrays.asList("Body weight", "Mass in kilograms"));
		ontologyTerms = Arrays.asList(standingHeight, bodyWeight);
		ontologyIds = Arrays.asList("1");
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

		List<String> ontologyIds = Arrays.asList("1");
		OntologyTerm standingHeight = OntologyTerm.create("id-height", "http://onto/height", "Standing height",
				Arrays.asList("Standing height", "length"));
		OntologyTerm bodyWeight = OntologyTerm.create("id-bmi", "http://onto/bmi", "Body weight",
				Arrays.asList("Body weight", "Mass in kilograms"));
		OntologyTerm hypertension = OntologyTerm.create("id-hyp", "http://onto/hyp", "Hypertension");
		OntologyTerm maternalHypertension = OntologyTerm.create("id-mhyp", "http://onto/mhyp", "Maternal hypertension");

		List<OntologyTerm> ontologyTerms = asList(standingHeight, bodyWeight, hypertension, maternalHypertension);
		Set<String> searchTerms = Sets.newLinkedHashSet(asList("history", "hypertension"));

		when(ontologyService.findOntologyTerms(ontologyIds, searchTerms, TagGroupGeneratorImpl.MAX_NUM_TAGS))
				.thenReturn(ontologyTerms);

		List<TagGroup> result = tagGroupGenerator.generateTagGroups("history hypertension", ontologyIds);

		assertEquals(result, asList(TagGroup.create(hypertension, "hypertens", 0.69231f)));
	}

	@Test
	public void testSearchDescription() throws InterruptedException, ExecutionException
	{
		Mockito.reset(ontologyService);

		List<String> ontologyIds = Arrays.asList("1");
		OntologyTerm standingHeight = OntologyTerm.create("id-height", "http://onto/height", "Standing height",
				Arrays.asList("Standing height", "length"));
		OntologyTerm bodyWeight = OntologyTerm.create("id-bmi", "http://onto/bmi", "Body weight",
				Arrays.asList("Body weight", "Mass in kilograms"));
		OntologyTerm hypertension = OntologyTerm.create("id-hyp", "http://onto/hyp", "Hypertension");
		OntologyTerm maternalHypertension = OntologyTerm.create("id-mhyp", "http://onto/mhyp", "Maternal hypertension");

		List<OntologyTerm> ontologyTerms = asList(standingHeight, bodyWeight, hypertension, maternalHypertension);
		Set<String> searchTerms = Sets.newLinkedHashSet(asList("standing", "height", "meters"));

		when(ontologyService.findOntologyTerms(ontologyIds, searchTerms, TagGroupGeneratorImpl.MAX_NUM_TAGS))
				.thenReturn(ontologyTerms);

		List<TagGroup> result = tagGroupGenerator.generateTagGroups("standing height meters", ontologyIds);

		assertEquals(result, asList(TagGroup.create(standingHeight, "stand height", 0.81250f)));
	}

	@Test
	public void testCreateOntologyTermPairwiseCombination()
	{
		OntologyTerm ot1 = OntologyTerm.create("1", "iri1", "septin 4", Arrays.asList("SEPT4"));
		OntologyTerm ot2 = OntologyTerm.create("2", "iri2", "4th of September", Arrays.asList("SEPT4"));
		OntologyTerm ot3 = OntologyTerm.create("3", "iri3", "National Security Agency", Arrays.asList("NSA"));
		OntologyTerm ot4 = OntologyTerm.create("4", "iri4", "National Security Advisor", Arrays.asList("NSA"));
		OntologyTerm ot5 = OntologyTerm.create("5", "iri5", "National Security Area", Arrays.asList("NSA"));
		OntologyTerm ot6 = OntologyTerm.create("6", "iri6", "	Activity", Arrays.asList("ACT"));

		Multimap<String, TagGroup> multiMap = LinkedListMultimap.create();

		multiMap.putAll("SEPT4",
				Arrays.asList(TagGroup.create(ot1, "SEPT4", 1.0f), TagGroup.create(ot2, "SEPT4", 1.0f)));

		multiMap.putAll("NSA", Arrays.asList(TagGroup.create(ot3, "NSA", 1.0f), TagGroup.create(ot4, "NSA", 1.0f),
				TagGroup.create(ot5, "NSA", 1.0f)));

		multiMap.putAll("ACT", Arrays.asList(TagGroup.create(ot6, "ACT", 1.0f)));

		List<List<OntologyTerm>> actual = tagGroupGenerator.createTagGroups(multiMap);
		List<List<OntologyTerm>> expected = Arrays.asList(Arrays.asList(ot1, ot3, ot6), Arrays.asList(ot1, ot4, ot6),
				Arrays.asList(ot1, ot5, ot6), Arrays.asList(ot2, ot3, ot6), Arrays.asList(ot2, ot4, ot6),
				Arrays.asList(ot2, ot5, ot6));

		Comparator<List<OntologyTerm>> comparator = new Comparator<List<OntologyTerm>>()
		{
			public int compare(List<OntologyTerm> o1, List<OntologyTerm> o2)
			{
				OntologyTerm combinedOt1 = OntologyTerm.and(o1.stream().toArray(OntologyTerm[]::new));
				OntologyTerm combinedOt2 = OntologyTerm.and(o2.stream().toArray(OntologyTerm[]::new));
				return combinedOt1.getIRI().compareTo(combinedOt2.getIRI());
			}
		};

		Collections.sort(actual, comparator);

		Collections.sort(expected, comparator);

		assertEquals(actual, expected);
	}

	@Test
	public void testCombineOntologyTerms()
	{
		OntologyTerm ot = OntologyTerm.create("02", "iri02", "weight", Arrays.asList("measured weight"));
		OntologyTerm ot0 = OntologyTerm.create("01", "iri01", "height",
				Arrays.asList("standing height", "body length"));
		OntologyTerm ot1 = OntologyTerm.create("1", "iri1", "septin 4", Arrays.asList("SEPT4"));
		OntologyTerm ot2 = OntologyTerm.create("2", "iri2", "4th of September", Arrays.asList("SEPT4"));
		OntologyTerm ot3 = OntologyTerm.create("3", "iri3", "National Security Agency", Arrays.asList("NSA"));
		OntologyTerm ot4 = OntologyTerm.create("4", "iri4", "National Security Advisor", Arrays.asList("NSA"));
		OntologyTerm ot5 = OntologyTerm.create("5", "iri5", "National Security Area", Arrays.asList("NSA"));
		OntologyTerm ot6 = OntologyTerm.create("6", "iri6", "Movement", Arrays.asList("Moved"));
		OntologyTerm ot7 = OntologyTerm.create("7", "iri7", "NSA movement SEPT4");

		Set<String> searchTerms = splitAndStem("NSA has a movement on SEPT4");

		List<OntologyTerm> relevantOntologyTerms = Lists.newArrayList(ot, ot0, ot1, ot2, ot3, ot4, ot5, ot6, ot7);

		// Randomize the order of the ontology terms
		Collections.shuffle(relevantOntologyTerms);

		List<TagGroup> ontologyTermHits = tagGroupGenerator.applyTagMatchingCriterion(relevantOntologyTerms,
				searchTerms, STRICT_MATCHING_CRITERION);

		List<TagGroup> combineTagGroups = tagGroupGenerator.combineTagGroups(searchTerms, ontologyTermHits);

		List<OntologyTerm> actualOntologyTerms = combineTagGroups.stream().map(TagGroup::getCombinedOntologyTerm)
				.collect(toList());

		List<OntologyTerm> expected = Lists.newArrayList(ot7, and(ot6, ot1, ot4), and(ot6, ot2, ot4),
				and(ot6, ot1, ot3), and(ot6, ot2, ot3), and(ot6, ot1, ot5), and(ot6, ot2, ot5));

		assertTrue(combineTagGroups.stream().allMatch(hit -> hit.getScore() == 0.92683f));

		Comparator<OntologyTerm> comparator = new Comparator<OntologyTerm>()
		{
			public int compare(OntologyTerm o1, OntologyTerm o2)
			{
				return o1.getIRI().compareTo(o2.getIRI());
			}
		};
		Collections.sort(actualOntologyTerms, comparator);

		Collections.sort(expected, comparator);

		assertEquals(actualOntologyTerms, expected);
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
		attribute.setDescription("Standing height (Ångstrøm)");

		when(ontologyService.findOntologyTerms(ontologyIds, newLinkedHashSet(asList("standing", "height", "ångstrøm")),
				TagGroupGeneratorImpl.MAX_NUM_TAGS)).thenReturn(ontologyTerms);

		List<TagGroup> result = tagGroupGenerator.generateTagGroups(attribute.getDescription(), ontologyIds);

		assertEquals(result.size(), 1);
		assertEquals(result.get(0), TagGroup.create(standingHeight, "stand height", 0.76471f));
	}

	@Test
	public void testSearchUnicode() throws InterruptedException, ExecutionException
	{
		Mockito.reset(ontologyService);
		attribute.setDescription("/əˈnædrəməs/");

		when(ontologyService.findOntologyTerms(ontologyIds, ImmutableSet.of("əˈnædrəməs"), MAX_NUMBER_ATTRIBTUES))
				.thenReturn(ontologyTerms);

		List<TagGroup> result = tagGroupGenerator.generateTagGroups(attribute.getDescription(), ontologyIds);

		assertEquals(result, Collections.emptyList());
	}

	@Test
	public void testSearchMultipleTags() throws InterruptedException, ExecutionException
	{
		Mockito.reset(ontologyService);
		attribute.setDescription("Body mass index");

		when(ontologyService.findOntologyTerms(ontologyIds, newLinkedHashSet(asList("body", "mass", "index")),
				MAX_NUMBER_ATTRIBTUES)).thenReturn(ontologyTerms);

		List<TagGroup> result = tagGroupGenerator.generateTagGroups(attribute.getDescription(), ontologyIds);

		assertEquals(result, Collections.emptyList());
	}

	@Test
	public void testSearchLabel() throws InterruptedException, ExecutionException
	{
		Mockito.reset(ontologyService);
		attribute.setDescription("Standing height (m.)");

		when(ontologyService.findOntologyTerms(ontologyIds, newLinkedHashSet(asList("standing", "height", "m.")),
				TagGroupGeneratorImpl.MAX_NUM_TAGS)).thenReturn(ontologyTerms);

		List<TagGroup> result = tagGroupGenerator.generateTagGroups(attribute.getDescription(), ontologyIds);

		TagGroup ontologyTermHit = TagGroup.create(standingHeight, "stand height", 0.92857f);

		assertEquals(result, Arrays.asList(ontologyTermHit));
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
		OntologyTagService ontologyTagService()
		{
			return mock(OntologyTagService.class);
		}

		@Bean
		TagGroupGeneratorImpl tagGroupGenerator()
		{
			return new TagGroupGeneratorImpl(ontologyService());
		}
	}
}
