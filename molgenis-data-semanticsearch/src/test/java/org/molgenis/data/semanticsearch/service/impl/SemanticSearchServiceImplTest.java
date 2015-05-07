package org.molgenis.data.semanticsearch.service.impl;

import static com.google.common.collect.ImmutableSet.of;
import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.mockito.Mockito;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.semanticsearch.semantic.Hit;
import org.molgenis.data.semanticsearch.service.SemanticSearchService;
import org.molgenis.data.semanticsearch.string.Stemmer;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.ontology.core.service.OntologyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;

@ContextConfiguration(classes = SemanticSearchServiceImplTest.Config.class)
public class SemanticSearchServiceImplTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private MetaDataService metaDataService;

	@Autowired
	private OntologyService ontologyService;

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
	public void testSearchIsoLatin() throws InterruptedException, ExecutionException
	{
		Mockito.reset(ontologyService);
		attribute.setDescription("Standing height (Ångstrøm)");

		when(ontologyService.findOntologyTerms(ontologies, of("standing", "height", "ångstrøm"), 100)).thenReturn(
				ontologyTerms);
		Hit<OntologyTerm> result = semanticSearchService.findTags(attribute, ontologies);
		assertEquals(result, Hit.<OntologyTerm> create(standingHeight, 0.74286f));
	}

	@Test
	public void testSearchUnicode() throws InterruptedException, ExecutionException
	{
		Mockito.reset(ontologyService);
		attribute.setDescription("/əˈnædrəməs/");

		when(ontologyService.findOntologyTerms(ontologies, of("əˈnædrəməs"), 100)).thenReturn(ontologyTerms);
		Hit<OntologyTerm> result = semanticSearchService.findTags(attribute, ontologies);
		assertEquals(result, null);
	}

	@Test
	public void testSearchMultipleTags() throws InterruptedException, ExecutionException
	{
		Mockito.reset(ontologyService);
		attribute.setDescription("Body mass index");

		when(ontologyService.findOntologyTerms(ontologies, of("body", "mass", "index"), 100)).thenReturn(ontologyTerms);
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
	}

}
