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

public class OntologyTermRepositoryTest
{
	OntologyLoader loader;
	OntologyTermRepository repository;

	@BeforeMethod
	public void setUp() throws OWLOntologyCreationException
	{
		loader = new OntologyLoader("ontology-test", new File("src/test/resources/test-ontology-loader.owl"));
		repository = new OntologyTermRepository(loader, "Test");
	}

	@Test
	public void getAttributes()
	{
		assertEquals(Iterables.size(repository.getAttributes()), 9);
	}

	@Test
	public void count()
	{
		assertEquals(repository.count(), 12);
	}

	@Test
	public void iterator()
	{
		Iterator<Entity> it = repository.iterator();
		assertTrue(it.hasNext());
		Entity tuple0 = it.next();
		assertEquals(tuple0.get("ontologyTerm"), "Person label test");
		assertEquals(tuple0.get("entity_type"), "ontologyTerm");

		assertTrue(it.hasNext());
		Entity tuple1 = it.next();
		assertEquals(tuple1.get("ontologyTerm"), "Person label test");
		assertEquals(tuple1.get("ontologyTermSynonym"), "People");
		assertEquals(tuple1.get("entity_type"), "ontologyTerm");

		assertTrue(it.hasNext());
		Entity tuple2 = it.next();
		assertEquals(tuple2.get("ontologyTermSynonym"), "Strange childhood");

		assertTrue(it.hasNext());
		Entity tuple3 = it.next();
		assertEquals(tuple3.get("ontologyTerm"), "Child");

		assertTrue(it.hasNext());
		Entity tuple4 = it.next();
		assertEquals(tuple4.get("ontologyTermSynonym"), "Son");

		assertTrue(it.hasNext());
		Entity tuple5 = it.next();
		assertEquals(tuple5.get("ontologyTermSynonym"), "Daughter");

		assertTrue(it.hasNext());
		Entity tuple6 = it.next();
		assertEquals(tuple6.get("ontologyTerm"), "Parent");

		assertTrue(it.hasNext());
		Entity tuple7 = it.next();
		assertEquals(tuple7.get("ontologyTermSynonym"), "Dad");

		assertTrue(it.hasNext());
		Entity tuple8 = it.next();
		assertEquals(tuple8.get("ontologyTermSynonym"), "Father");
		assertEquals(tuple8.get("ontologyTermIRI"), "http://harmonization/test/owl/1.0.0#Father");
	}
}
