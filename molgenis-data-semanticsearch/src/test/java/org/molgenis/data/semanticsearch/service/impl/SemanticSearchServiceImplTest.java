package org.molgenis.data.semanticsearch.service.impl;

import static com.google.common.collect.ImmutableSet.of;
import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.mockito.Mockito;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.semanticsearch.semantic.Hit;
import org.molgenis.data.semanticsearch.service.SemanticSearchService;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.ontology.core.service.OntologyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
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
	private SemanticSearchService semanticSearchService;

	private List<String> ontologies;

	private OntologyTerm standingHeight;

	private List<OntologyTerm> ontologyTerms;

	private DefaultAttributeMetaData attribute;

	@BeforeTest
	public void beforeTest()
	{
		ontologies = asList("1", "2");
		standingHeight = OntologyTerm.create("http://onto/height", "Standing height");
		ontologyTerms = asList(standingHeight);
		attribute = new DefaultAttributeMetaData("attr1");
	}

	@Test
	public void testSearchDescription() throws InterruptedException, ExecutionException
	{
		Mockito.reset(ontologyService);
		attribute.setDescription("Standing height in meters.");
		when(
				ontologyService.findOntologyTerms(ontologies, ImmutableSet.<String> of("standing", "height", "meters"),
						100)).thenReturn(ontologyTerms);
		List<Hit<OntologyTerm>> terms = semanticSearchService.findTags(attribute, ontologies);
		assertEquals(terms, asList(Hit.<OntologyTerm> create(standingHeight, 2.0f / 3)));
	}

	@Test
	public void testSearchLabel() throws InterruptedException, ExecutionException
	{
		Mockito.reset(ontologyService);
		attribute.setDescription("Standing height (m.)");

		when(ontologyService.findOntologyTerms(ontologies, ImmutableSet.<String> of("standing", "height", "m"), 100))
				.thenReturn(ontologyTerms);
		List<Hit<OntologyTerm>> terms = semanticSearchService.findTags(attribute, ontologies);
		assertEquals(terms, asList(Hit.<OntologyTerm> create(standingHeight, 0.85714f)));
	}

	@Test
	public void testSearchIsoLatin() throws InterruptedException, ExecutionException
	{
		Mockito.reset(ontologyService);
		attribute.setDescription("Standing height (Ångstrøm)");

		when(ontologyService.findOntologyTerms(ontologies, of("standing", "height", "ångstrøm"), 100)).thenReturn(
				ontologyTerms);
		List<Hit<OntologyTerm>> terms = semanticSearchService.findTags(attribute, ontologies);
		assertEquals(terms, asList(Hit.<OntologyTerm> create(standingHeight, 0.54762f)));
	}

	@Test
	public void testSearchUnicode() throws InterruptedException, ExecutionException
	{
		Mockito.reset(ontologyService);
		attribute.setDescription("/əˈnædrəməs/");

		when(ontologyService.findOntologyTerms(ontologies, of("əˈnædrəməs"), 100)).thenReturn(ontologyTerms);
		List<Hit<OntologyTerm>> terms = semanticSearchService.findTags(attribute, ontologies);
		assertEquals(terms, asList(Hit.<OntologyTerm> create(standingHeight, 0.08333f)));
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
