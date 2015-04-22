package org.molgenis.data.semanticsearch.service.impl;

import static com.google.common.collect.ImmutableSet.of;
import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.QueryRule;
import org.molgenis.data.meta.AttributeMetaDataMetaData;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.semanticsearch.service.SemanticSearchService;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.QueryImpl;
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
	private SemanticSearchServiceHelper semanticSearchServiceHelper;

	@Autowired
	private DataService dataService;

	@Autowired
	private OntologyTagService ontologyTagService;

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

	/**
	 * Test description. . See the method {@link SemanticSearchService#findTags}
	 * 
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
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

	/**
	 * Test label. See the method {@link SemanticSearchService#findTags}
	 * 
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	@Test
	public void testSearchLabel() throws InterruptedException, ExecutionException
	{
		this.testSearchLabel();
		attribute.setLabel("Standing height (m.)");
		when(ontologyService.findOntologyTerms(ontologies, ImmutableSet.<String> of("standing", "height", "m"), 100))
				.thenReturn(ontologyTerms);
		List<OntologyTerm> terms = semanticSearchService.findTags(attribute, ontologies);
		assertEquals(terms, ontologyTerms);
	}

	/**
	 * Test SearchIsoLatin. See the method {@link SemanticSearchService#findTags}
	 * 
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	@Test
	public void testSearchIsoLatin() throws InterruptedException, ExecutionException
	{
		attribute.setLabel("Standing height (Ångstrøm)");

		when(ontologyService.findOntologyTerms(ontologies, of("standing", "height", "ångstrøm"), 100)).thenReturn(
				ontologyTerms);
		List<OntologyTerm> terms = semanticSearchService.findTags(attribute, ontologies);
		assertEquals(terms, ontologyTerms);
	}

	/**
	 * Test: List<OntologyTerm> findTags(AttributeMetaData attribute, List<String> ontologyIds);
	 * 
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	@Test
	public void testSearchUnicode() throws InterruptedException, ExecutionException
	{
		attribute.setLabel("/əˈnædrəməs/");

		when(ontologyService.findOntologyTerms(ontologies, of("ə", "nædrəməs"), 100)).thenReturn(ontologyTerms);
		List<OntologyTerm> terms = semanticSearchService.findTags(attribute, ontologies);
		assertEquals(terms, ontologyTerms);
	}
	

	@Test
	public void testFindAttributes()
	{
		EntityMetaData sourceEntityMetaData = new DefaultEntityMetaData("sourceEntityMetaData");
		EntityMetaData targetEntityMetaData = new DefaultEntityMetaData("targetEntityMetaData");
		DefaultAttributeMetaData targetAttribute = new DefaultAttributeMetaData("targetAttribute");

		List<String> attributeIdentifiers = Arrays.asList("1", "2");
		when(semanticSearchServiceHelper.getAttributeIdentifiers(sourceEntityMetaData))
				.thenReturn(attributeIdentifiers);
		
		QueryRule createDisMaxQueryRule = new QueryRule(); // TODO JJ
		when(semanticSearchServiceHelper.createDisMaxQueryRule(targetEntityMetaData, targetAttribute)).thenReturn(
				createDisMaxQueryRule);
		
		List<QueryRule> disMaxQueryRules = new ArrayList<QueryRule>(); // TODO JJ
		Iterable<Entity> attributeMetaDataEntities = new ArrayList<Entity>(); // TODO JJ
		when(dataService.findAll(AttributeMetaDataMetaData.ENTITY_NAME, new QueryImpl(disMaxQueryRules))).thenReturn(
				attributeMetaDataEntities);

		// TODO jj test this
		// return Iterables.size(attributeMetaDataEntities) > 0 ? MetaUtils.toExistingAttributeMetaData(
		// sourceEntityMetaData, attributeMetaDataEntities) : sourceEntityMetaData.getAttributes();

		// Iterable<AttributeMetaData> terms = semanticSearchService.findAttributes(sourceEntityMetaData,
		// targetEntityMetaData, targetAttribute);

		// TODO JJ test terms
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
		SemanticSearchServiceHelper semanticSearchServiceHelper()
		{
			return mock(SemanticSearchServiceHelper.class);
		}
	}
}
