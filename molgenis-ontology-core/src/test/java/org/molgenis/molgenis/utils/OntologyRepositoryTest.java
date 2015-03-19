package org.molgenis.molgenis.utils;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.elasticsearch.common.collect.Iterables;
import org.molgenis.data.Entity;
import org.molgenis.ontology.repository.OntologyIndexRepository;
import org.molgenis.ontology.utils.OntologyLoader;
import org.molgenis.ontology.utils.ZipFileUtil;
import org.molgenis.util.ResourceUtils;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class OntologyRepositoryTest
{
	OntologyIndexRepository repository;
	OntologyLoader loader;

	@BeforeMethod
	public void setUp() throws OWLOntologyCreationException, FileNotFoundException, IOException
	{
		File file = ResourceUtils.getFile("test-ontology-loader.owl.zip");
		List<File> uploadedFiles = ZipFileUtil.unzip(file);
		loader = new OntologyLoader("ontology-test", uploadedFiles.get(0));
		repository = new OntologyIndexRepository(loader, "test");
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
