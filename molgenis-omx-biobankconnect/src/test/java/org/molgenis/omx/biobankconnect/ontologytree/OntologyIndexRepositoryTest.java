package org.molgenis.omx.biobankconnect.ontologytree;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.molgenis.data.Entity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.biobankconnect.ontology.repository.OntologyIndexRepository;
import org.molgenis.omx.biobankconnect.ontology.repository.OntologyQueryRepository;
import org.molgenis.omx.biobankconnect.ontology.repository.OntologyTermIndexRepository;
import org.molgenis.omx.biobankconnect.ontologyservice.OntologyService;
import org.molgenis.search.Hit;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchResult;
import org.molgenis.search.SearchService;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class OntologyIndexRepositoryTest
{
	OntologyQueryRepository ontologyIndexRepository;

	@BeforeClass
	public void setUp() throws OWLOntologyCreationException
	{
		Map<String, Object> columnValueMap1 = new HashMap<String, Object>();
		columnValueMap1.put(OntologyTermIndexRepository.ENTITY_TYPE, OntologyIndexRepository.TYPE_ONTOLOGY);
		columnValueMap1.put(OntologyTermIndexRepository.ONTOLOGY_IRI, "http://www.ontology.test");
		columnValueMap1.put(OntologyTermIndexRepository.ONTOLOGY_NAME, "test ontology");
		Hit hit1 = mock(Hit.class);
		when(hit1.getId()).thenReturn("ontology-1");
		when(hit1.getColumnValueMap()).thenReturn(columnValueMap1);

		Map<String, Object> columnValueMap2 = new HashMap<String, Object>();
		columnValueMap2.put(OntologyTermIndexRepository.ENTITY_TYPE, OntologyIndexRepository.TYPE_ONTOLOGY);
		columnValueMap2.put(OntologyTermIndexRepository.ONTOLOGY_IRI, "http://www.another.ontology.test");
		columnValueMap2.put(OntologyTermIndexRepository.ONTOLOGY_NAME, "another ontology");
		Hit hit2 = mock(Hit.class);
		when(hit2.getId()).thenReturn("ontology-2");
		when(hit2.getColumnValueMap()).thenReturn(columnValueMap2);

		Map<String, Object> columnValueMap3 = new HashMap<String, Object>();
		columnValueMap3.put(OntologyTermIndexRepository.ENTITY_TYPE, OntologyIndexRepository.TYPE_ONTOLOGY);
		columnValueMap3.put(OntologyTermIndexRepository.ONTOLOGY_IRI, "http://www.final.ontology.test");
		columnValueMap3.put(OntologyTermIndexRepository.ONTOLOGY_NAME, "final ontology");
		Hit hit3 = mock(Hit.class);
		when(hit3.getId()).thenReturn("ontology-3");
		when(hit3.getColumnValueMap()).thenReturn(columnValueMap3);

		OntologyService ontologyService = mock(OntologyService.class);

		SearchService searchService = mock(SearchService.class);
		when(
				searchService.search(new SearchRequest(null, new QueryImpl().eq(OntologyIndexRepository.ENTITY_TYPE,
						OntologyIndexRepository.TYPE_ONTOLOGY), null))).thenReturn(
				new SearchResult(3, Arrays.asList(hit1, hit2, hit3)));

		when(
				searchService.search(new SearchRequest(null, new QueryImpl()
						.eq(OntologyIndexRepository.ONTOLOGY_IRI, "http://www.final.ontology.test").and()
						.eq(OntologyIndexRepository.ENTITY_TYPE, OntologyIndexRepository.TYPE_ONTOLOGY), null)))
				.thenReturn(new SearchResult(3, Arrays.asList(hit3)));

		when(
				searchService.count(null,
						new QueryImpl().eq(OntologyIndexRepository.ENTITY_TYPE, OntologyIndexRepository.TYPE_ONTOLOGY)
								.pageSize(Integer.MAX_VALUE).offset(Integer.MIN_VALUE))).thenReturn(new Long(3));
		when(
				searchService.count(null,
						new QueryImpl().eq(OntologyIndexRepository.ONTOLOGY_IRI, "http://www.final.ontology.test")
								.and().eq(OntologyIndexRepository.ENTITY_TYPE, OntologyIndexRepository.TYPE_ONTOLOGY)
								.pageSize(Integer.MAX_VALUE).offset(Integer.MIN_VALUE))).thenReturn(new Long(1));

		when(searchService.searchById(null, "ontology-2")).thenReturn(hit2);

		ontologyIndexRepository = new OntologyQueryRepository("ontologyindex", ontologyService, searchService);
	}

	@Test
	public void count()
	{
		assertEquals(ontologyIndexRepository.count(new QueryImpl()), 3);
		assertEquals(ontologyIndexRepository.count(new QueryImpl().eq(OntologyIndexRepository.ONTOLOGY_IRI,
				"http://www.final.ontology.test")), 1);
	}

	@Test
	public void findAll()
	{
		List<String> validOntologyIris = Arrays.asList("http://www.ontology.test", "http://www.another.ontology.test",
				"http://www.final.ontology.test");

		for (Entity entity : ontologyIndexRepository.findAll(new QueryImpl()))
		{
			assertTrue(validOntologyIris.contains(entity.get(OntologyIndexRepository.ONTOLOGY_IRI).toString()));
		}
	}

	@Test
	public void findOneQuery()
	{
		Entity entity = ontologyIndexRepository.findOne(new QueryImpl().eq(OntologyIndexRepository.ONTOLOGY_IRI,
				"http://www.final.ontology.test"));
		assertEquals(entity.get(OntologyIndexRepository.ONTOLOGY_NAME).toString(), "final ontology");
	}

	@Test
	public void findOneObject()
	{
		Entity entity = ontologyIndexRepository.findOne("ontology-2");
		assertEquals(entity.get(OntologyIndexRepository.ONTOLOGY_NAME).toString(), "another ontology");
	}

	@Test
	public void getUrl()
	{
		assertEquals("ontologyindex://ontologyindex", ontologyIndexRepository.getUrl());
	}
}
