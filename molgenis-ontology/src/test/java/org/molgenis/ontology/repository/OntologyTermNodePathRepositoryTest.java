package org.molgenis.ontology.repository;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.molgenis.data.Entity;
import org.molgenis.data.support.UuidGenerator;
import org.molgenis.ontology.model.OntologyTermNodePathMetaData;
import org.molgenis.ontology.utils.OntologyLoader;
import org.molgenis.ontology.utils.ZipFileUtil;
import org.molgenis.util.ResourceUtils;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class OntologyTermNodePathRepositoryTest
{
	private static OntologyLoader ontologyLoader;
	private static UuidGenerator uuidGenerator;
	private static OntologyTermNodePathRepository ontologyTermNodePathRepository;

	@BeforeClass
	public static void beforeClass() throws FileNotFoundException, IOException, OWLOntologyCreationException
	{
		File file = ResourceUtils.getFile("small_test_data.owl.zip");
		List<File> uploadedFiles = ZipFileUtil.unzip(file);
		ontologyLoader = new OntologyLoader("small_test_data", uploadedFiles.get(0));
		uuidGenerator = new UuidGenerator();
		ontologyTermNodePathRepository = new OntologyTermNodePathRepository(ontologyLoader, uuidGenerator);
	}

	@Test
	public void ontologyRepositoryIterator() throws OWLOntologyCreationException
	{

		Iterator<Entity> i = ontologyTermNodePathRepository.iterator();
		assertTrue(i.hasNext());
		Entity entity = i.next();
		assertNotNull(entity.get(OntologyTermNodePathMetaData.ID));
		assertEquals(entity.get(OntologyTermNodePathMetaData.ONTOLOGY_TERM_NODE_PATH), "0[0]");
		assertTrue(entity.getBoolean(OntologyTermNodePathMetaData.ROOT));

		entity = i.next();
		assertNotNull(entity.get(OntologyTermNodePathMetaData.ID));
		assertEquals(entity.get(OntologyTermNodePathMetaData.ONTOLOGY_TERM_NODE_PATH), "0[0].0[1]");
		assertFalse(entity.getBoolean(OntologyTermNodePathMetaData.ROOT));

		entity = i.next();
		assertNotNull(entity.get(OntologyTermNodePathMetaData.ID));
		assertEquals(entity.get(OntologyTermNodePathMetaData.ONTOLOGY_TERM_NODE_PATH), "0[0].0[1].0[2]");
		assertFalse(entity.getBoolean(OntologyTermNodePathMetaData.ROOT));

		entity = i.next();
		assertNotNull(entity.get(OntologyTermNodePathMetaData.ID));
		assertEquals(entity.get(OntologyTermNodePathMetaData.ONTOLOGY_TERM_NODE_PATH), "0[0].0[1].0[2].0[3]");
		assertFalse(entity.getBoolean(OntologyTermNodePathMetaData.ROOT));

		entity = i.next();
		assertNotNull(entity.get(OntologyTermNodePathMetaData.ID));
		assertEquals(entity.get(OntologyTermNodePathMetaData.ONTOLOGY_TERM_NODE_PATH), "0[0].1[1]");
		assertFalse(entity.getBoolean(OntologyTermNodePathMetaData.ROOT));

		entity = i.next();
		assertNotNull(entity.get(OntologyTermNodePathMetaData.ID));
		assertEquals(entity.get(OntologyTermNodePathMetaData.ONTOLOGY_TERM_NODE_PATH), "0[0].1[1].0[2]");
		assertFalse(entity.getBoolean(OntologyTermNodePathMetaData.ROOT));

		assertFalse(i.hasNext());
	}
}
