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
import org.molgenis.ontology.repository.OntologyTermIndexRepository;
import org.molgenis.ontology.utils.OntologyLoader;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class OntologyTermIndexRepositoryTest
{
	OntologyLoader loader;
	OntologyTermIndexRepository repository;

	@BeforeMethod
	public void setUp() throws OWLOntologyCreationException
	{
		URL url = Thread.currentThread().getContextClassLoader().getResource("test-ontology-loader.owl");
		File file = new File(url.getPath());

		loader = new OntologyLoader("ontology-test", file);
		repository = new OntologyTermIndexRepository(loader, "Test", mock(SearchService.class));
	}

	@Test
	public void getAttributes()
	{
		assertEquals(Iterables.size(repository.getEntityMetaData().getAttributes()), 15);
	}

	@Test
	public void iterator() throws OWLOntologyCreationException
	{

		Iterator<Entity> it = repository.iterator();
		assertTrue(it.hasNext());
		it.next();

		assertTrue(it.hasNext());
		Entity tuple0 = it.next();
		assertEquals(tuple0.get("ontologyTerm"), "Person label test");
		assertEquals(tuple0.get("entity_type"), "ontologyTerm");

		assertTrue(it.hasNext());
		Entity tuple1 = it.next();
		assertEquals(tuple1.get("ontologyTerm"), "Person label test");
		assertEquals(tuple1.get("ontologyTermSynonym"), "Person label test");
		assertEquals(tuple1.get("entity_type"), "ontologyTerm");
		assertEquals(tuple1.get("ontologyTermIRI"), "http://harmonization/test/owl/1.0.0#Person");

		assertTrue(it.hasNext());
		Entity tuple2 = it.next();
		assertEquals(tuple2.get("ontologyTermSynonym"), "Parent");
		assertEquals(tuple2.get("ontologyTermIRI"), "http://harmonization/test/owl/1.0.0#Parent");

		assertTrue(it.hasNext());
		Entity tuple3 = it.next();
		assertEquals(tuple3.get("ontologyTerm"), "Father");
		assertEquals(tuple3.get("ontologyTermIRI"), "http://harmonization/test/owl/1.0.0#Father");

		assertTrue(it.hasNext());
		Entity tuple4 = it.next();
		assertEquals(tuple4.get("ontologyTermSynonym"), "Mother");
		assertEquals(tuple4.get("ontologyTermIRI"), "http://harmonization/test/owl/1.0.0#Mother");

		assertTrue(it.hasNext());
		Entity tuple5 = it.next();
		assertEquals(tuple5.get("ontologyTermSynonym"), "Mummy");
		assertEquals(tuple5.get("ontologyTermIRI"), "http://harmonization/test/owl/1.0.0#Mother");

		assertTrue(it.hasNext());
		Entity tuple6 = it.next();
		assertEquals(tuple6.get("ontologyTerm"), "Father");
		assertEquals(tuple6.get("ontologyTermIRI"), "http://harmonization/test/owl/1.0.0#Father");

		assertTrue(it.hasNext());
		Entity tuple7 = it.next();
		assertEquals(tuple7.get("ontologyTermSynonym"), "Dad");
		assertEquals(tuple7.get("ontologyTermIRI"), "http://harmonization/test/owl/1.0.0#Father");

		assertTrue(it.hasNext());
		Entity tuple8 = it.next();
		assertEquals(tuple8.get("ontologyTermSynonym"), "Child");
		assertEquals(tuple8.get("ontologyTermIRI"), "http://harmonization/test/owl/1.0.0#Child");

		assertTrue(it.hasNext());
		Entity tuple9 = it.next();
		assertEquals(tuple9.get("ontologyTermSynonym"), "Daughter");
		assertEquals(tuple9.get("ontologyTermIRI"), "http://harmonization/test/owl/1.0.0#Daughter");

		assertTrue(it.hasNext());
		Entity tuple10 = it.next();
		assertEquals(tuple10.get("ontologyTermSynonym"), "Son");
		assertEquals(tuple10.get("ontologyTermIRI"), "http://harmonization/test/owl/1.0.0#Son");

		assertTrue(it.hasNext());
		Entity tuple11 = it.next();
		assertEquals(tuple11.get("ontologyTermSynonym"), "Strange childhood");
		assertEquals(tuple11.get("ontologyTermIRI"), "http://harmonization/test/owl/1.0.0#Child");

	}

}
