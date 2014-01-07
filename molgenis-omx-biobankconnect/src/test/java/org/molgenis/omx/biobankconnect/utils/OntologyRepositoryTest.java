package org.molgenis.omx.biobankconnect.utils;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.util.Iterator;

import org.elasticsearch.common.collect.Iterables;
import org.molgenis.data.Entity;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class OntologyRepositoryTest
{
	OntologyRepository repository;
	OntologyLoader loader;

	@BeforeMethod
	public void setUp() throws OWLOntologyCreationException
	{
		loader = new OntologyLoader("ontology-test", new File("src/test/resources/test-ontology-loader.owl"));
		repository = new OntologyRepository(loader, "test");
	}

	@Test
	public void getAllColumns()
	{
		assertEquals(Iterables.size(repository.getAttributes()), 3);
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
		assertEquals(entity.get("url"), "http://harmonization/test/owl/1.0.0");
		assertEquals(entity.get("entity_type"), "indexedOntology");
		assertEquals(entity.get("ontologyLabel"), "ontology-test");
	}
}
