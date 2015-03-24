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
import org.molgenis.ontology.model.OntologyTermDynamicAnnotationMetaData;
import org.molgenis.ontology.utils.OntologyLoader;
import org.molgenis.ontology.utils.ZipFileUtil;
import org.molgenis.util.ResourceUtils;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class OntologyTermDynamicAnnotationRepositoryTest
{
	private static OntologyLoader ontologyLoader;
	private static UuidGenerator uuidGenerator;
	private static OntologyTermDynamicAnnotationRepository ontologyTermDynamicAnnotationRepository;

	@BeforeClass
	public static void beforeClass() throws FileNotFoundException, IOException, OWLOntologyCreationException
	{
		File file = ResourceUtils.getFile("small_test_data.owl.zip");
		List<File> uploadedFiles = ZipFileUtil.unzip(file);
		ontologyLoader = new OntologyLoader("small_test_data", uploadedFiles.get(0));
		uuidGenerator = new UuidGenerator();
		ontologyTermDynamicAnnotationRepository = new OntologyTermDynamicAnnotationRepository(ontologyLoader,
				uuidGenerator);
	}

	@Test
	public void ontologyTermDynamicAnnotationRepositoryIterator() throws OWLOntologyCreationException
	{

		Iterator<Entity> i = ontologyTermDynamicAnnotationRepository.iterator();
		assertTrue(i.hasNext());
		Entity entity = i.next();
		assertNotNull(entity.get(OntologyTermDynamicAnnotationMetaData.ID));
		assertEquals(entity.get(OntologyTermDynamicAnnotationMetaData.NAME), "friday");
		assertEquals(entity.get(OntologyTermDynamicAnnotationMetaData.VALUE), "2412423");
		assertEquals(entity.get(OntologyTermDynamicAnnotationMetaData.LABEL), "friday:2412423");

		assertTrue(i.hasNext());
		entity = i.next();
		assertNotNull(entity.get(OntologyTermDynamicAnnotationMetaData.ID));
		assertEquals(entity.get(OntologyTermDynamicAnnotationMetaData.NAME), "molgenis");
		assertEquals(entity.get(OntologyTermDynamicAnnotationMetaData.VALUE), "1231424");
		assertEquals(entity.get(OntologyTermDynamicAnnotationMetaData.LABEL), "molgenis:1231424");

		assertTrue(i.hasNext());
		entity = i.next();
		assertNotNull(entity.get(OntologyTermDynamicAnnotationMetaData.ID));
		assertEquals(entity.get(OntologyTermDynamicAnnotationMetaData.NAME), "GCC");
		assertEquals(entity.get(OntologyTermDynamicAnnotationMetaData.VALUE), "987654");
		assertEquals(entity.get(OntologyTermDynamicAnnotationMetaData.LABEL), "GCC:987654");

		assertTrue(i.hasNext());
		entity = i.next();
		assertNotNull(entity.get(OntologyTermDynamicAnnotationMetaData.ID));
		assertEquals(entity.get(OntologyTermDynamicAnnotationMetaData.NAME), "GCC");
		assertEquals(entity.get(OntologyTermDynamicAnnotationMetaData.VALUE), "123456");
		assertEquals(entity.get(OntologyTermDynamicAnnotationMetaData.LABEL), "GCC:123456");

		assertFalse(i.hasNext());
	}
}
