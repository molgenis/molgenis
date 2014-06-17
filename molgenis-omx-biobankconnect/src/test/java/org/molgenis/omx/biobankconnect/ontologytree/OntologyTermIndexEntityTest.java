package org.molgenis.omx.biobankconnect.ontologytree;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.biobankconnect.ontologyservice.OntologyService;
import org.molgenis.omx.biobankconnect.utils.OntologyRepository;
import org.molgenis.omx.biobankconnect.utils.OntologyTermRepository;
import org.molgenis.omx.observ.Characteristic;
import org.molgenis.search.Hit;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchResult;
import org.molgenis.search.SearchService;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class OntologyTermIndexEntityTest
{

	EntityMetaData entityMetaData;

	OntologyTermIndexEntity ontologyTermIndexEntity;

	@BeforeClass
	public void setUp() throws OWLOntologyCreationException
	{
		Map<String, Object> columnValueMap1 = new HashMap<String, Object>();
		columnValueMap1.put(OntologyTermRepository.ROOT, true);
		columnValueMap1.put(OntologyTermRepository.LAST, false);
		columnValueMap1.put(OntologyTermRepository.NODE_PATH, "1");
		columnValueMap1.put(OntologyRepository.ONTOLOGY_URL, "http://www.ontology.test");
		columnValueMap1.put(OntologyTermRepository.ONTOLOGY_TERM_IRI, "http://www.ontology.test#term1");
		Hit hit1 = mock(Hit.class);
		when(hit1.getId()).thenReturn("forged-id");
		when(hit1.getColumnValueMap()).thenReturn(columnValueMap1);

		Map<String, Object> columnValueMap2 = new HashMap<String, Object>();
		columnValueMap2.put(OntologyTermRepository.ROOT, false);
		columnValueMap2.put(OntologyTermRepository.LAST, true);
		columnValueMap2.put(OntologyTermRepository.NODE_PATH, "1.2");
		columnValueMap2.put(OntologyTermRepository.PARENT_NODE_PATH, "1");
		columnValueMap2.put(OntologyTermRepository.PARENT_ONTOLOGY_TERM_URL, "http://www.ontology.test");
		columnValueMap2.put(OntologyRepository.ONTOLOGY_URL, "http://www.ontology.test");
		columnValueMap2.put(OntologyTermRepository.ONTOLOGY_TERM_IRI, "http://www.ontology.test#term2");
		Hit hit2 = mock(Hit.class);
		when(hit2.getId()).thenReturn("forged-id-2");
		when(hit2.getColumnValueMap()).thenReturn(columnValueMap2);

		Map<String, Object> columnValueMap3 = new HashMap<String, Object>();
		columnValueMap3.put(OntologyTermRepository.ROOT, false);
		columnValueMap3.put(OntologyTermRepository.LAST, true);
		columnValueMap3.put(OntologyTermRepository.NODE_PATH, "1.3");
		columnValueMap3.put(OntologyTermRepository.PARENT_NODE_PATH, "1");
		columnValueMap3.put(OntologyTermRepository.PARENT_ONTOLOGY_TERM_URL, "http://www.ontology.test");
		columnValueMap3.put(OntologyRepository.ONTOLOGY_URL, "http://www.ontology.test");
		columnValueMap3.put(OntologyTermRepository.ONTOLOGY_TERM_IRI, "http://www.ontology.test#term3");
		Hit hit3 = mock(Hit.class);
		when(hit3.getId()).thenReturn("forged-id-3");
		when(hit3.getColumnValueMap()).thenReturn(columnValueMap3);

		SearchService searchService = mock(SearchService.class);
		when(
				searchService.search(new SearchRequest(OntologyService
						.createOntologyTermDocumentType("http://www.ontology.test"), new QueryImpl().pageSize(10000)
						.eq(OntologyTermRepository.ROOT, true), null))).thenReturn(
				new SearchResult(1, Arrays.asList(hit1)));

		when(
				searchService.search(new SearchRequest(OntologyService
						.createOntologyTermDocumentType("http://www.ontology.test"), new QueryImpl()
						.eq(OntologyTermRepository.PARENT_NODE_PATH, "1").and()
						.eq(OntologyTermRepository.PARENT_ONTOLOGY_TERM_URL, "http://www.ontology.test#term1")
						.pageSize(500), null))).thenReturn(new SearchResult(2, Arrays.asList(hit2, hit3)));

		OntologyIndexRepository ontologyIndexRepository = mock(OntologyIndexRepository.class);
		ontologyTermIndexEntity = new OntologyTermIndexEntity(hit1, ontologyIndexRepository.getEntityMetaData(),
				searchService);
	}

	@Test
	public void get()
	{
		assertEquals(ontologyTermIndexEntity.get(Characteristic.ID).toString(), "forged-id");
		assertEquals(Boolean.parseBoolean(ontologyTermIndexEntity.get(OntologyTermRepository.ROOT).toString()), true);
		Object attributes = ontologyTermIndexEntity.get("attributes");
		if (attributes instanceof List<?>)
		{
			assertEquals(((List<?>) attributes).size(), 2);
			List<String> ids = Arrays.asList("forged-id-2", "forged-id-3");
			for (Object object : (List<?>) attributes)
			{
				if (object instanceof OntologyTermIndexEntity)
				{
					assertTrue(ids.contains(((OntologyTermIndexEntity) object).getIdValue().toString()));
				}
			}
		}
	}
}