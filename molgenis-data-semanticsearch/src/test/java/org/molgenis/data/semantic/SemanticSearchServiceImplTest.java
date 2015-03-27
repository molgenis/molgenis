package org.molgenis.data.semantic;

import static com.google.common.collect.ImmutableSet.of;
import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.List;
import java.util.concurrent.ExecutionException;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.ontology.OntologyService;
import org.molgenis.ontology.repository.model.OntologyTerm;
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
		attribute.setDescription("Standing height in meters.");
		when(
				ontologyService.findOntologyTerms(ontologies, ImmutableSet.<String> of("standing", "height", "meters"),
						100)).thenReturn(ontologyTerms);
		List<OntologyTerm> terms = semanticSearchService.findTags(attribute, ontologies);
		assertEquals(terms, ontologyTerms);
	}

	@Test
	public void testSearchLabel() throws InterruptedException, ExecutionException
	{
		attribute.setLabel("Standing height (m.)");

		when(ontologyService.findOntologyTerms(ontologies, ImmutableSet.<String> of("standing", "height", "m"), 100))
				.thenReturn(ontologyTerms);
		List<OntologyTerm> terms = semanticSearchService.findTags(attribute, ontologies);
		assertEquals(terms, ontologyTerms);
	}

	@Test
	public void testSearchIsoLatin() throws InterruptedException, ExecutionException
	{
		attribute.setLabel("Standing height (Ångstrøm)");

		when(ontologyService.findOntologyTerms(ontologies, of("standing", "height", "ångstrøm"), 100)).thenReturn(
				ontologyTerms);
		List<OntologyTerm> terms = semanticSearchService.findTags(attribute, ontologies);
		assertEquals(terms, ontologyTerms);
	}

	@Test
	public void testSearchUnicode() throws InterruptedException, ExecutionException
	{
		attribute.setLabel("/əˈnædrəməs/");

		when(ontologyService.findOntologyTerms(ontologies, of("ə", "nædrəməs"), 100)).thenReturn(ontologyTerms);
		List<OntologyTerm> terms = semanticSearchService.findTags(attribute, ontologies);
		assertEquals(terms, ontologyTerms);
	}
}
