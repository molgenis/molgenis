package org.molgenis.omx.biobankconnect.utils;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.util.Iterator;

import org.molgenis.framework.tupletable.TableException;
import org.molgenis.util.tuple.Tuple;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class OntologyTableTest
{
	OntologyTable table;
	OntologyLoader loader;

	@BeforeMethod
	public void setUp() throws OWLOntologyCreationException
	{
		loader = new OntologyLoader("ontology-test", new File("src/test/resources/test-ontology-loader.owl"));
		table = new OntologyTable(loader);
	}

	@Test
	public void getAllColumns() throws TableException
	{
		assertEquals(table.getAllColumns().size(), 3);
	}

	@Test
	public void getCount()
	{
		assertEquals(table.getCount(), 1);
	}

	@Test
	public void iterator()
	{
		Iterator<Tuple> it = table.iterator();

		assertTrue(it.hasNext());
		Tuple tuple0 = it.next();
		assertEquals(tuple0.get("url"), "http://harmonization/test/owl/1.0.0");
		assertEquals(tuple0.get("entity_type"), "indexedOntology");
		assertEquals(tuple0.get("ontologyLabel"), "ontology-test");
	}
}
