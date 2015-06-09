package org.molgenis.ontology.core.repository;

import static java.util.Arrays.asList;
import static org.elasticsearch.common.collect.ImmutableSet.of;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.mockito.ArgumentCaptor;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.core.meta.OntologyTermMetaData;
import org.molgenis.ontology.core.meta.OntologyTermNodePathMetaData;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;

@ContextConfiguration(classes = OntologyTermRepositoryTest.Config.class)
public class OntologyTermRepositoryTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	DataService dataService;

	@Autowired
	OntologyTermRepository ontologyTermRepository;

	private Entity ontologyTermEntity;

	@BeforeTest
	public void beforeTest()
	{
		ontologyTermEntity = new MapEntity(OntologyTermMetaData.INSTANCE);
		ontologyTermEntity.set(OntologyTermMetaData.ID, "12");
		ontologyTermEntity.set(OntologyTermMetaData.ONTOLOGY, "34");
		ontologyTermEntity.set(OntologyTermMetaData.ONTOLOGY_TERM_IRI, "http://www.test.nl/iri");
		ontologyTermEntity.set(OntologyTermMetaData.ONTOLOGY_TERM_NAME, "Ontology term");
	}

	@Test
	public void testFindOntologyTerms()
	{
		ArgumentCaptor<Query> queryCaptor = forClass(Query.class);
		when(dataService.findAll(eq(OntologyTermMetaData.ENTITY_NAME), queryCaptor.capture())).thenReturn(
				asList(ontologyTermEntity));

		List<OntologyTerm> terms = ontologyTermRepository.findOntologyTerms(asList("1", "2"),
				of("term1", "term2", "term3"), 100);

		assertEquals(
				terms,
				asList(OntologyTerm.create("http://www.test.nl/iri", "Ontology term", null,
						Arrays.asList("Ontology term"))));
		assertEquals(
				queryCaptor.getValue().toString(),
				"QueryImpl [rules=[ontology IN '[1, 2]',  AND , ( search 'term1' OR  search 'term2' OR  search 'term3')], pageSize=100, offset=0, sort=null]");

	}

	@Test
	public void testGetChildOntologyTermsByNodePath()
	{
		Entity nodePathEntity_1 = new MapEntity(ImmutableMap.of(OntologyTermNodePathMetaData.ONTOLOGY_TERM_NODE_PATH,
				"0[0].1[1]"));
		Entity nodePathEntity_2 = new MapEntity(ImmutableMap.of(OntologyTermNodePathMetaData.ONTOLOGY_TERM_NODE_PATH,
				"0[0].1[1].0[2]"));
		Entity nodePathEntity_3 = new MapEntity(ImmutableMap.of(OntologyTermNodePathMetaData.ONTOLOGY_TERM_NODE_PATH,
				"0[0].1[1].1[2]"));

		when(
				dataService.findAll(OntologyTermNodePathMetaData.ENTITY_NAME,
						new QueryImpl().like(OntologyTermNodePathMetaData.ONTOLOGY_TERM_NODE_PATH, "0[0].1[1]")))
				.thenReturn(Arrays.asList(nodePathEntity_1, nodePathEntity_2, nodePathEntity_3));

		MapEntity ontologyTerm_2 = new MapEntity();
		ontologyTerm_2.set(OntologyTermMetaData.ONTOLOGY_TERM_IRI, "iri 2");
		ontologyTerm_2.set(OntologyTermMetaData.ONTOLOGY_TERM_NAME, "name 2");
		ontologyTerm_2.set(OntologyTermMetaData.ONTOLOGY_TERM_NODE_PATH, nodePathEntity_2);
		ontologyTerm_2.set(OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM, Collections.emptyList());

		MapEntity ontologyTerm_3 = new MapEntity();
		ontologyTerm_3.set(OntologyTermMetaData.ONTOLOGY_TERM_IRI, "iri 3");
		ontologyTerm_3.set(OntologyTermMetaData.ONTOLOGY_TERM_NAME, "name 3");
		ontologyTerm_3.set(OntologyTermMetaData.ONTOLOGY_TERM_NODE_PATH, nodePathEntity_3);
		ontologyTerm_3.set(OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM, Collections.emptyList());

		when(
				dataService.findAll(
						OntologyTermMetaData.ENTITY_NAME,
						new QueryImpl().in(OntologyTermNodePathMetaData.ONTOLOGY_TERM_NODE_PATH,
								Arrays.asList(nodePathEntity_2, nodePathEntity_3)))).thenReturn(
				Arrays.asList(ontologyTerm_2, ontologyTerm_3));

		List<OntologyTerm> childOntologyTermsByNodePath = ontologyTermRepository
				.getChildOntologyTermsByNodePath(nodePathEntity_1);
		assertEquals(childOntologyTermsByNodePath.size(), 2);
		assertEquals(childOntologyTermsByNodePath.get(0),
				OntologyTerm.create("iri 2", "name 2", null, Arrays.asList("name 2")));
		assertEquals(childOntologyTermsByNodePath.get(1),
				OntologyTerm.create("iri 3", "name 3", null, Arrays.asList("name 3")));
	}

	@Test
	public void testCalculateNodePathDistance()
	{
		// Case 1
		assertEquals(ontologyTermRepository.calculateNodePathDistance("0[0].0[1]", "0[0].0[1].1[2]"), 1);

		// Case 2
		assertEquals(ontologyTermRepository.calculateNodePathDistance("0[0].0[1].1[2]", "0[0].0[1]"), 1);

		// Case 3
		assertEquals(ontologyTermRepository.calculateNodePathDistance("0[0].0[1].1[2].2[3]", "0[0].0[1].0[2].2[3]"), 4);

		// Case 4
		assertEquals(ontologyTermRepository.calculateNodePathDistance("0[0].0[1]", "0[0].0[1].0[2].1[3].2[4]"), 3);

		// Case 5
		assertEquals(ontologyTermRepository.calculateNodePathDistance("0[0].0[1]", "0[0].0[1]"), 0);
	}

	@Test
	public void testGetOntologyTerm()
	{
		when(
				dataService.findOne(OntologyTermMetaData.ENTITY_NAME,
						QueryImpl.EQ(OntologyTermMetaData.ONTOLOGY_TERM_IRI, "http://www.test.nl/iri"))).thenReturn(
				ontologyTermEntity);

		String[] iris =
		{ "http://www.test.nl/iri" };

		OntologyTerm ontologyTerm = ontologyTermRepository.getOntologyTerm(iris);
		assertEquals(ontologyTerm,
				OntologyTerm.create("http://www.test.nl/iri", "Ontology term", Arrays.asList("Ontology term")));
	}

	@Configuration
	public static class Config
	{
		@Bean
		public DataService dataService()
		{
			return mock(DataService.class);
		}

		@Bean
		public OntologyTermRepository ontologyTermRepository()
		{
			return new OntologyTermRepository();
		}
	}
}
