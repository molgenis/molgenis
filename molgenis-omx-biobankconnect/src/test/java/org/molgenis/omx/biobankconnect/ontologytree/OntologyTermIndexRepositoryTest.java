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
import org.molgenis.omx.biobankconnect.ontologyservice.OntologyService;
import org.molgenis.omx.biobankconnect.utils.OntologyTermRepository;
import org.molgenis.search.Hit;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchResult;
import org.molgenis.search.SearchService;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class OntologyTermIndexRepositoryTest
{
	OntologyTermIndexRepository ontologyTermIndexRepository;
	String ontologyIRI = "http://www.ontology.test";

	@BeforeClass
	public void setUp() throws OWLOntologyCreationException
	{

		Map<String, Object> columnValueMap1 = new HashMap<String, Object>();
		columnValueMap1.put(OntologyTermRepository.ENTITY_TYPE, OntologyTermRepository.TYPE_ONTOLOGYTERM);
		columnValueMap1.put(OntologyTermRepository.ONTOLOGY_IRI, ontologyIRI);
		columnValueMap1.put(OntologyTermRepository.ONTOLOGY_LABEL, "test ontology");
		columnValueMap1.put(OntologyTermRepository.LAST, false);
		columnValueMap1.put(OntologyTermRepository.ROOT, true);
		columnValueMap1.put(OntologyTermRepository.NODE_PATH, "1.2");
		columnValueMap1.put(OntologyTermRepository.ONTOLOGY_TERM_IRI, ontologyIRI + "#term1");
		columnValueMap1.put(OntologyTermRepository.ONTOLOGY_TERM, "ontology term 1");
		columnValueMap1.put(OntologyTermRepository.SYNONYMS, "OT-1");
		Hit hit1 = mock(Hit.class);
		when(hit1.getId()).thenReturn("ontology-1");
		when(hit1.getColumnValueMap()).thenReturn(columnValueMap1);

		Map<String, Object> columnValueMap2 = new HashMap<String, Object>();
		columnValueMap2.put(OntologyTermRepository.ENTITY_TYPE, OntologyTermRepository.TYPE_ONTOLOGYTERM);
		columnValueMap2.put(OntologyTermRepository.ONTOLOGY_IRI, ontologyIRI);
		columnValueMap2.put(OntologyTermRepository.ONTOLOGY_LABEL, "test ontology");
		columnValueMap2.put(OntologyTermRepository.LAST, false);
		columnValueMap2.put(OntologyTermRepository.ROOT, false);
		columnValueMap2.put(OntologyTermRepository.NODE_PATH, "1.2.3");
		columnValueMap2.put(OntologyTermRepository.PARENT_NODE_PATH, "1.2");
		columnValueMap2.put(OntologyTermRepository.PARENT_ONTOLOGY_TERM_URL, ontologyIRI + "#term1");
		columnValueMap2.put(OntologyTermRepository.ONTOLOGY_TERM_IRI, ontologyIRI + "#term2");
		columnValueMap2.put(OntologyTermRepository.ONTOLOGY_TERM, "ontology term 2");
		columnValueMap2.put(OntologyTermRepository.SYNONYMS, "OT-2");
		Hit hit2 = mock(Hit.class);
		when(hit2.getId()).thenReturn("ontology-2");
		when(hit2.getColumnValueMap()).thenReturn(columnValueMap2);

		Map<String, Object> columnValueMap3 = new HashMap<String, Object>();
		columnValueMap3.put(OntologyTermRepository.ENTITY_TYPE, OntologyTermRepository.TYPE_ONTOLOGYTERM);
		columnValueMap3.put(OntologyTermRepository.ONTOLOGY_IRI, ontologyIRI);
		columnValueMap3.put(OntologyTermRepository.ONTOLOGY_LABEL, "test ontology");
		columnValueMap3.put(OntologyTermRepository.LAST, false);
		columnValueMap3.put(OntologyTermRepository.ROOT, false);
		columnValueMap3.put(OntologyTermRepository.NODE_PATH, "1.2.4");
		columnValueMap3.put(OntologyTermRepository.PARENT_NODE_PATH, "1.2");
		columnValueMap3.put(OntologyTermRepository.PARENT_ONTOLOGY_TERM_URL, ontologyIRI + "#term1");
		columnValueMap3.put(OntologyTermRepository.ONTOLOGY_TERM_IRI, ontologyIRI + "#term3");
		columnValueMap3.put(OntologyTermRepository.ONTOLOGY_TERM, "ontology term 3");
		columnValueMap3.put(OntologyTermRepository.SYNONYMS, "OT-3");
		Hit hit3 = mock(Hit.class);
		when(hit3.getId()).thenReturn("ontology-3");
		when(hit3.getColumnValueMap()).thenReturn(columnValueMap3);

		SearchService searchService = mock(SearchService.class);
		when(
				searchService.search(new SearchRequest(OntologyService.createOntologyTermDocumentType(ontologyIRI),
						new QueryImpl()
								.eq(OntologyTermRepository.ENTITY_TYPE, OntologyTermRepository.TYPE_ONTOLOGYTERM), null)))
				.thenReturn(new SearchResult(3, Arrays.asList(hit1, hit2, hit3)));

		when(
				searchService.search(new SearchRequest(OntologyService.createOntologyTermDocumentType(ontologyIRI),
						new QueryImpl().eq(OntologyTermRepository.PARENT_NODE_PATH, "1.2").and()
								.eq(OntologyTermRepository.ENTITY_TYPE, OntologyTermRepository.TYPE_ONTOLOGYTERM), null)))
				.thenReturn(new SearchResult(2, Arrays.asList(hit2, hit3)));

		when(
				searchService.search(new SearchRequest(OntologyService.createOntologyTermDocumentType(ontologyIRI),
						new QueryImpl().eq(OntologyTermRepository.NODE_PATH, "1.2.3").and()
								.eq(OntologyTermRepository.ENTITY_TYPE, OntologyTermRepository.TYPE_ONTOLOGYTERM), null)))
				.thenReturn(new SearchResult(1, Arrays.asList(hit2)));

		when(searchService.searchById(OntologyService.createOntologyTermDocumentType(ontologyIRI), "ontology-3"))
				.thenReturn(hit3);

		ontologyTermIndexRepository = new OntologyTermIndexRepository("test-ontology", ontologyIRI, searchService);
	}

	@Test
	public void count()
	{
		assertEquals(ontologyTermIndexRepository.count(new QueryImpl()), 3);
	}

	@Test
	public void findAll()
	{
		for (Entity entity : ontologyTermIndexRepository.findAll(new QueryImpl()))
		{
			Object rootObject = entity.get(OntologyTermRepository.ROOT);
			if (rootObject != null && Boolean.parseBoolean(rootObject.toString()))
			{
				List<String> validOntologyTermIris = Arrays.asList(ontologyIRI + "#term2", ontologyIRI + "#term3");
				for (Entity subEntity : ontologyTermIndexRepository.findAll(new QueryImpl().eq(
						OntologyTermRepository.PARENT_NODE_PATH, entity.get(OntologyTermRepository.NODE_PATH)
								.toString())))
				{
					assertTrue(validOntologyTermIris.contains(subEntity.get(OntologyTermRepository.ONTOLOGY_TERM_IRI)
							.toString()));
				}
			}
		}
	}

	@Test
	public void findOneQuery()
	{
		Entity entity = ontologyTermIndexRepository.findOne(new QueryImpl().eq(OntologyTermRepository.NODE_PATH,
				"1.2.3"));
		assertEquals(entity.get(OntologyTermRepository.SYNONYMS).toString(), "OT-2");
	}

	@Test
	public void findOneObject()
	{
		Entity entity = ontologyTermIndexRepository.findOne("ontology-3");
		assertEquals(entity.get(OntologyTermRepository.SYNONYMS).toString(), "OT-3");
	}

	@Test
	public void getUrl()
	{
		assertEquals(ontologyTermIndexRepository.getUrl(),
				"ontologytermindex://" + ontologyTermIndexRepository.getName());
	}
}
