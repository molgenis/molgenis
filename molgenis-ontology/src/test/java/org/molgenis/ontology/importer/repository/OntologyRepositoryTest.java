package org.molgenis.ontology.importer.repository;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.molgenis.data.Entity;
import org.molgenis.data.support.UuidGenerator;
import org.molgenis.ontology.core.meta.OntologyMetaData;
import org.molgenis.ontology.importer.repository.OntologyRepository;
import org.molgenis.ontology.utils.OntologyLoader;
import org.molgenis.ontology.utils.ZipFileUtil;
import org.molgenis.util.ResourceUtils;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class OntologyRepositoryTest
{
	private static OntologyLoader ontologyLoader;
	private static UuidGenerator uuidGenerator;
	private static OntologyRepository ontologyRepository;

	@BeforeClass
	public static void beforeClass() throws FileNotFoundException, IOException, OWLOntologyCreationException
	{
		File file = ResourceUtils.getFile("small_test_data.owl.zip");
		List<File> uploadedFiles = ZipFileUtil.unzip(file);
		ontologyLoader = new OntologyLoader("small_test_data", uploadedFiles.get(0));
		uuidGenerator = new UuidGenerator();
		ontologyRepository = new OntologyRepository(ontologyLoader, uuidGenerator);
	}

	@Test
	public void ontologyRepositoryIterator() throws OWLOntologyCreationException
	{

		Iterator<Entity> i = ontologyRepository.iterator();
		assertTrue(i.hasNext());
		Entity entity = i.next();
		assertEquals(entity.get(OntologyMetaData.ONTOLOGY_IRI), "http://www.molgenis.org");
		assertEquals(entity.get(OntologyMetaData.ONTOLOGY_NAME), "small_test_data");
		assertFalse(i.hasNext());
	}
}
