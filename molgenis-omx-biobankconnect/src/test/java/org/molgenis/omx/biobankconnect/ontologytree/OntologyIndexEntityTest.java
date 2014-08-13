package org.molgenis.omx.biobankconnect.ontologytree;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.biobankconnect.ontology.repository.OntologyIndexRepository;
import org.molgenis.omx.biobankconnect.ontology.repository.OntologyQueryRepository;
import org.molgenis.omx.biobankconnect.ontology.repository.OntologyTermIndexRepository;
import org.molgenis.omx.biobankconnect.ontologyindexer.AsyncOntologyIndexer;
import org.molgenis.omx.biobankconnect.ontologyservice.OntologyService;
import org.molgenis.omx.observ.Characteristic;
import org.molgenis.search.Hit;
import org.molgenis.search.SearchRequest;
import org.molgenis.search.SearchResult;
import org.molgenis.search.SearchService;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class OntologyIndexEntityTest
{
	EntityMetaData entityMetaData;

	OntologyEntity ontologyIndexEntity;

	@BeforeClass
	public void setUp() throws OWLOntologyCreationException
	{
		Map<String, Object> columnValueMap1 = new HashMap<String, Object>();
		columnValueMap1.put(OntologyTermIndexRepository.ROOT, true);
		columnValueMap1.put(OntologyIndexRepository.ONTOLOGY_IRI, "http://www.ontology.test");
		Hit hit1 = mock(Hit.class);
		when(hit1.getId()).thenReturn("forged-id");
		when(hit1.getColumnValueMap()).thenReturn(columnValueMap1);

		Map<String, Object> columnValueMap2 = new HashMap<String, Object>();
		columnValueMap2.put(OntologyTermIndexRepository.ROOT, true);
		columnValueMap2.put(OntologyIndexRepository.ONTOLOGY_IRI, "http://www.ontology.test");
		Hit hit2 = mock(Hit.class);
		when(hit2.getId()).thenReturn("forged-id-2");
		when(hit2.getColumnValueMap()).thenReturn(columnValueMap2);

		Map<String, Object> columnValueMap3 = new HashMap<String, Object>();
		columnValueMap3.put(OntologyTermIndexRepository.ROOT, true);
		columnValueMap3.put(OntologyIndexRepository.ONTOLOGY_IRI, "http://www.ontology.test");
		Hit hit3 = mock(Hit.class);
		when(hit3.getId()).thenReturn("forged-id-3");
		when(hit3.getColumnValueMap()).thenReturn(columnValueMap3);

		SearchService searchService = mock(SearchService.class);
		when(
				searchService.search(new SearchRequest(AsyncOntologyIndexer
						.createOntologyDocumentType("http://www.ontology.test"), new QueryImpl(), null))).thenReturn(
				new SearchResult(2, Arrays.asList(hit2, hit3)));
		when(
				searchService.search(new SearchRequest(AsyncOntologyIndexer
						.createOntologyTermDocumentType("http://www.ontology.test"), new QueryImpl().pageSize(10000)
						.eq(OntologyTermIndexRepository.ROOT, true), null))).thenReturn(
				new SearchResult(1, Arrays.asList(hit1)));

		OntologyService ontologyService = mock(OntologyService.class);
		when(ontologyService.getRootOntologyTerms("http://www.ontology.test")).thenReturn(
				Arrays.asList(hit1, hit2, hit3));

		OntologyQueryRepository ontologyIndexRepository = mock(OntologyQueryRepository.class);

		ontologyIndexEntity = new OntologyEntity(hit1, ontologyIndexRepository.getEntityMetaData(), ontologyService,
				searchService);
	}

	@Test
	public void get()
	{
		assertEquals(ontologyIndexEntity.get(Characteristic.ID).toString(), "forged-id");
		assertEquals(Boolean.parseBoolean(ontologyIndexEntity.get(OntologyTermIndexRepository.ROOT).toString()), true);
		assertEquals(ontologyIndexEntity.get(OntologyQueryRepository.FIELDTYPE).toString().toLowerCase(),
				MolgenisFieldTypes.COMPOUND.toString().toLowerCase());
		Object attribues = ontologyIndexEntity.get("attributes");
		if (attribues instanceof List<?>)
		{
			attribues = (List<?>) ontologyIndexEntity.get("attributes");
			assertEquals(((List<?>) attribues).size(), 3);
		}
	}
}
