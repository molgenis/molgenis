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
import org.molgenis.ontology.model.OntologyTermSynonymMetaData;
import org.molgenis.ontology.utils.OntologyLoader;
import org.molgenis.ontology.utils.ZipFileUtil;
import org.molgenis.util.ResourceUtils;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class OntologyTermSysnonymRepositoryTest
{
	private static OntologyLoader ontologyLoader;
	private static UuidGenerator uuidGenerator;
	private static OntologyTermSynonymRepository ontologyTermSynonymRepository;

	@BeforeClass
	public static void beforeClass() throws FileNotFoundException, IOException, OWLOntologyCreationException
	{
		File file = ResourceUtils.getFile("small_test_data.owl.zip");
		List<File> uploadedFiles = ZipFileUtil.unzip(file);
		ontologyLoader = new OntologyLoader("small_test_data", uploadedFiles.get(0));
		uuidGenerator = new UuidGenerator();
		ontologyTermSynonymRepository = new OntologyTermSynonymRepository(ontologyLoader, uuidGenerator);
	}

	@Test
	public void ontologyTermSynonymRepository() throws OWLOntologyCreationException
	{

		Iterator<Entity> i = ontologyTermSynonymRepository.iterator();
		assertTrue(i.hasNext());
		Entity entity = i.next();
		assertNotNull(entity.get(OntologyTermSynonymMetaData.ID));
		assertEquals(entity.get(OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM), "organization");

		assertTrue(i.hasNext());
		entity = i.next();
		assertNotNull(entity.get(OntologyTermSynonymMetaData.ID));
		assertEquals(entity.get(OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM), "team");

		assertTrue(i.hasNext());
		entity = i.next();
		assertNotNull(entity.get(OntologyTermSynonymMetaData.ID));
		assertEquals(entity.get(OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM), "Genomics coordination center");

		assertTrue(i.hasNext());
		entity = i.next();
		assertNotNull(entity.get(OntologyTermSynonymMetaData.ID));
		assertEquals(entity.get(OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM), "hospital");

		assertFalse(i.hasNext());
	}
}
