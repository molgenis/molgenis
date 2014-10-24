package org.molgenis.molgenis.utils;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.Iterator;

import org.elasticsearch.common.collect.Iterables;
import org.molgenis.data.Entity;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.ontology.repository.OntologyIndexRepository;
import org.molgenis.ontology.utils.OntologyLoader;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class OntologyRepositoryTest
{
	OntologyIndexRepository repository;
	OntologyLoader loader;

	@BeforeMethod
	public void setUp() throws OWLOntologyCreationException
	{
		URL url = Thread.currentThread().getContextClassLoader().getResource("test-ontology-loader.owl");
		File file = new File(url.getPath());

		loader = new OntologyLoader("ontology-test", file);
		repository = new OntologyIndexRepository(loader, "test", mock(SearchService.class));
	}

	@Test
	public void getAllColumns()
	{
		assertEquals(Iterables.size(repository.getEntityMetaData().getAttributes()), 15);
	}

	@Test
	public void getCount()
	{
		assertEquals(repository.count(), 1);
	}

	@Test
	public void iterator()
	{
		Iterator<Entity> it = repository.iterator();

		assertTrue(it.hasNext());
		Entity entity = it.next();
		assertEquals(entity.get(OntologyIndexRepository.ONTOLOGY_IRI), "http://harmonization/test/owl/1.0.0");
		assertEquals(entity.get(OntologyIndexRepository.ENTITY_TYPE), "indexedOntology");
		assertEquals(entity.get(OntologyIndexRepository.ONTOLOGY_NAME), "ontology-test");
	}
}
