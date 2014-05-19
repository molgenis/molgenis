package org.molgenis.omx.biobankconnect.ontologyservice;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.biobankconnect.utils.OntologyRepository;
import org.molgenis.omx.biobankconnect.utils.OntologyTermRepository;
import org.molgenis.omx.observ.target.Ontology;
import org.molgenis.search.Hit;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchResult;
import org.molgenis.search.SearchService;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class OntologyServiceTest
{
	OntologyService ontologyService;

	@BeforeClass
	public void setUp() throws OWLOntologyCreationException
	{
		Map<String, Object> columnValueMap1 = new HashMap<String, Object>();
		columnValueMap1.put(OntologyTermRepository.ENTITY_TYPE, OntologyRepository.TYPE_ONTOLOGY);
		columnValueMap1.put(OntologyTermRepository.ONTOLOGY_IRI, "http://www.ontology.test");
		columnValueMap1.put(OntologyTermRepository.ONTOLOGY_LABEL, "test ontology");
		Hit hit1 = mock(Hit.class);
		when(hit1.getId()).thenReturn("ontology-1");
		when(hit1.getColumnValueMap()).thenReturn(columnValueMap1);

		Map<String, Object> columnValueMap2 = new HashMap<String, Object>();
		columnValueMap2.put(OntologyTermRepository.ENTITY_TYPE, OntologyRepository.TYPE_ONTOLOGY);
		columnValueMap2.put(OntologyTermRepository.ONTOLOGY_IRI, "http://www.another.ontology.test");
		columnValueMap2.put(OntologyTermRepository.ONTOLOGY_LABEL, "another ontology");
		Hit hit2 = mock(Hit.class);
		when(hit2.getId()).thenReturn("ontology-2");
		when(hit2.getColumnValueMap()).thenReturn(columnValueMap2);

		Map<String, Object> columnValueMap3 = new HashMap<String, Object>();
		columnValueMap3.put(OntologyTermRepository.ENTITY_TYPE, OntologyTermRepository.TYPE_ONTOLOGYTERM);
		columnValueMap3.put(OntologyTermRepository.ONTOLOGY_IRI, "http://www.ontology.test");
		columnValueMap3.put(OntologyTermRepository.ONTOLOGY_LABEL, "test ontology");
		columnValueMap3.put(OntologyTermRepository.LAST, false);
		columnValueMap3.put(OntologyTermRepository.ROOT, true);
		columnValueMap3.put(OntologyTermRepository.NODE_PATH, "1.2");
		columnValueMap3.put(OntologyTermRepository.ONTOLOGY_TERM_IRI, "http://www.ontology.test#term1");
		columnValueMap3.put(OntologyTermRepository.ONTOLOGY_TERM, "ontology term 1");
		columnValueMap3.put(OntologyTermRepository.SYNONYMS, "OT-1");
		Hit hit3 = mock(Hit.class);
		when(hit3.getId()).thenReturn("ontologyterm-1");
		when(hit3.getColumnValueMap()).thenReturn(columnValueMap3);

		Map<String, Object> columnValueMap4 = new HashMap<String, Object>();
		columnValueMap4.put(OntologyTermRepository.ENTITY_TYPE, OntologyTermRepository.TYPE_ONTOLOGYTERM);
		columnValueMap4.put(OntologyTermRepository.ONTOLOGY_IRI, "http://www.ontology.test");
		columnValueMap4.put(OntologyTermRepository.ONTOLOGY_LABEL, "test ontology");
		columnValueMap4.put(OntologyTermRepository.LAST, false);
		columnValueMap4.put(OntologyTermRepository.ROOT, false);
		columnValueMap4.put(OntologyTermRepository.NODE_PATH, "1.2.3");
		columnValueMap4.put(OntologyTermRepository.PARENT_NODE_PATH, "1.2");
		columnValueMap4.put(OntologyTermRepository.PARENT_ONTOLOGY_TERM_URL, "http://www.ontology.test#term1");
		columnValueMap4.put(OntologyTermRepository.ONTOLOGY_TERM_IRI, "http://www.ontology.test#term2");
		columnValueMap4.put(OntologyTermRepository.ONTOLOGY_TERM, "ontology term 2");
		columnValueMap4.put(OntologyTermRepository.SYNONYMS, "OntologyTerm two");
		columnValueMap4.put("score", 2.5);
		Hit hit4 = mock(Hit.class);
		when(hit4.getId()).thenReturn("ontologyterm-2");
		when(hit4.getColumnValueMap()).thenReturn(columnValueMap4);

		Map<String, Object> columnValueMap5 = new HashMap<String, Object>();
		columnValueMap5.put(OntologyTermRepository.ENTITY_TYPE, OntologyTermRepository.TYPE_ONTOLOGYTERM);
		columnValueMap5.put(OntologyTermRepository.ONTOLOGY_IRI, "http://www.ontology.test");
		columnValueMap5.put(OntologyTermRepository.ONTOLOGY_LABEL, "test ontology");
		columnValueMap5.put(OntologyTermRepository.LAST, false);
		columnValueMap5.put(OntologyTermRepository.ROOT, false);
		columnValueMap5.put(OntologyTermRepository.NODE_PATH, "1.2.4");
		columnValueMap5.put(OntologyTermRepository.PARENT_NODE_PATH, "1.2");
		columnValueMap5.put(OntologyTermRepository.PARENT_ONTOLOGY_TERM_URL, "http://www.ontology.test#term1");
		columnValueMap5.put(OntologyTermRepository.ONTOLOGY_TERM_IRI, "http://www.ontology.test#term3");
		columnValueMap5.put(OntologyTermRepository.ONTOLOGY_TERM, "ontology term 3");
		columnValueMap5.put(OntologyTermRepository.SYNONYMS, "OntologyTerm three");
		columnValueMap5.put("score", 4.5);
		Hit hit5 = mock(Hit.class);
		when(hit5.getId()).thenReturn("ontologyterm-3");
		when(hit5.getColumnValueMap()).thenReturn(columnValueMap5);

		SearchService searchService = mock(SearchService.class);
		when(
				searchService.search(new SearchRequest(null, new QueryImpl().eq(OntologyRepository.ENTITY_TYPE,
						OntologyRepository.TYPE_ONTOLOGY).pageSize(1000), null))).thenReturn(
				new SearchResult(2, Arrays.asList(hit1, hit2)));

		when(
				searchService.search(new SearchRequest(OntologyService
						.createOntologyDocumentType("http://www.ontology.test"), new QueryImpl()
						.eq(OntologyRepository.ENTITY_TYPE, OntologyRepository.TYPE_ONTOLOGY).and()
						.eq(OntologyRepository.ONTOLOGY_URL, "http://www.ontology.test").pageSize(1000), null)))
				.thenReturn(new SearchResult(1, Arrays.asList(hit1)));
		when(
				searchService.search(new SearchRequest(OntologyService
						.createOntologyTermDocumentType("http://www.ontology.test"), new QueryImpl()
						.eq(OntologyTermRepository.NODE_PATH, "1.2").and()
						.eq(OntologyTermRepository.ONTOLOGY_TERM_IRI, "http://www.ontology.test#term1").pageSize(5000),
						null))).thenReturn(new SearchResult(1, Arrays.asList(hit3)));

		when(
				searchService.search(new SearchRequest(OntologyService
						.createOntologyTermDocumentType("http://www.ontology.test"), new QueryImpl()
						.eq(OntologyTermRepository.PARENT_NODE_PATH, "1.2").and()
						.eq(OntologyTermRepository.PARENT_ONTOLOGY_TERM_URL, "http://www.ontology.test#term1")
						.pageSize(5000), null))).thenReturn(new SearchResult(2, Arrays.asList(hit4, hit5)));

		when(
				searchService.search(new SearchRequest(OntologyService
						.createOntologyTermDocumentType("http://www.ontology.test"), new QueryImpl().eq(
						OntologyTermRepository.ROOT, true).pageSize(10000), null))).thenReturn(
				new SearchResult(1, Arrays.asList(hit3)));

		QueryImpl query = new QueryImpl();
		query.addRule(new QueryRule(OntologyTermRepository.SYNONYMS, Operator.SEARCH, "ontologyterm"));
		query.addRule(new QueryRule(Operator.OR));
		query.addRule(new QueryRule(OntologyTermRepository.SYNONYMS, Operator.SEARCH, "three"));
		query.pageSize(100);
		when(
				searchService.search(new SearchRequest(OntologyService
						.createOntologyTermDocumentType("http://www.ontology.test"), query, null))).thenReturn(
				new SearchResult(2, Arrays.asList(hit4, hit5)));

		ontologyService = new OntologyService(searchService);
	}

	@Test
	public void createOntologyDocumentType()
	{
		assertEquals(OntologyService.createOntologyDocumentType("http://www.ontology.test"),
				"ontology-http://www.ontology.test");
	}

	@Test
	public void createOntologyTermDocumentType()
	{
		assertEquals(OntologyService.createOntologyTermDocumentType("http://www.ontology.test"),
				"ontologyTerm-http://www.ontology.test");
	}

	@Test
	public void findOntologyTerm()
	{
		Hit hit = ontologyService.findOntologyTerm("http://www.ontology.test", "http://www.ontology.test#term1", "1.2");
		assertEquals(hit.getId(), "ontologyterm-1");
	}

	@Test
	public void getAllOntologies()
	{
		List<String> validOntologyIris = Arrays.asList("http://www.ontology.test", "http://www.another.ontology.test");
		for (Ontology ontology : ontologyService.getAllOntologies())
		{
			assertTrue(validOntologyIris.contains(ontology.getOntologyURI()));
		}
	}

	@Test
	public void getChildren()
	{
		Hit parent = ontologyService.findOntologyTerm("http://www.ontology.test", "http://www.ontology.test#term1",
				"1.2");
		String ontologyIri = parent.getColumnValueMap().get(OntologyTermRepository.ONTOLOGY_IRI).toString();
		String parentOntologyTermPath = parent.getColumnValueMap().get(OntologyTermRepository.NODE_PATH).toString();
		String parentOntologyTermIri = parent.getColumnValueMap().get(OntologyTermRepository.ONTOLOGY_TERM_IRI)
				.toString();
		List<Hit> children = ontologyService.getChildren(ontologyIri, parentOntologyTermIri, parentOntologyTermPath);
		assertEquals(children.size(), 2);
		List<String> validedOntologyTermIri = Arrays.asList("http://www.ontology.test#term2",
				"http://www.ontology.test#term3");
		for (Hit hit : children)
		{
			assertTrue(validedOntologyTermIri.contains(hit.getColumnValueMap()
					.get(OntologyTermRepository.ONTOLOGY_TERM_IRI).toString()));
		}
	}

	@Test
	public void getOntologyByUrl()
	{
		Hit hit = ontologyService.getOntologyByUrl("http://www.ontology.test");
		assertEquals(hit.getId(), "ontology-1");

	}

	@Test
	public void getRootOntologyTerms()
	{
		List<Hit> rootHits = ontologyService.getRootOntologyTerms("http://www.ontology.test");
		assertEquals(rootHits.size(), 1);
		assertEquals(rootHits.get(0).getId(), "ontologyterm-1");
	}

	@Test
	public void search()
	{
		SearchResult result = ontologyService.search("http://www.ontology.test", "OntologyTerm three");
		List<Hit> searchHits = result.getSearchHits();
		assertEquals(searchHits.size(), 2);
		assertEquals(searchHits.get(0).getId(), "ontologyterm-3");
		assertEquals(searchHits.get(1).getId(), "ontologyterm-2");
	}
}
