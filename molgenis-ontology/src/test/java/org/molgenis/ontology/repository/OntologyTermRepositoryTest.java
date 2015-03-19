package org.molgenis.ontology.repository;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.support.UuidGenerator;
import org.molgenis.ontology.model.OntologyMetaData;
import org.molgenis.ontology.model.OntologyTermDynamicAnnotationMetaData;
import org.molgenis.ontology.model.OntologyTermMetaData;
import org.molgenis.ontology.model.OntologyTermNodePathMetaData;
import org.molgenis.ontology.model.OntologyTermSynonymMetaData;
import org.molgenis.ontology.utils.OntologyLoader;
import org.molgenis.ontology.utils.ZipFileUtil;
import org.molgenis.util.ResourceUtils;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class OntologyTermRepositoryTest
{
	private static OntologyLoader ontologyLoader;
	private static UuidGenerator uuidGenerator;
	private static OntologyTermRepository ontologyTermRepository;
	private static OntologyTermDynamicAnnotationRepository ontologyTermDynamicAnnotationRepository;
	private static OntologyTermNodePathRepository ontologyTermNodePathRepository;
	private static OntologyTermSynonymRepository ontologyTermSynonymRepository;

	@Autowired
	private static DataService dataService;

	@BeforeClass
	public static void beforeClass() throws FileNotFoundException, IOException, OWLOntologyCreationException
	{
		File file = ResourceUtils.getFile("small_test_data.owl.zip");
		List<File> uploadedFiles = ZipFileUtil.unzip(file);
		ontologyLoader = new OntologyLoader("small_test_data", uploadedFiles.get(0));
		uuidGenerator = new UuidGeneratorTest();
		ontologyTermDynamicAnnotationRepository = new OntologyTermDynamicAnnotationRepository(ontologyLoader,
				uuidGenerator);
		ontologyTermNodePathRepository = new OntologyTermNodePathRepository(ontologyLoader, uuidGenerator);
		ontologyTermSynonymRepository = new OntologyTermSynonymRepository(ontologyLoader, uuidGenerator);

		populateRepositoy(ontologyTermDynamicAnnotationRepository.iterator());
		populateRepositoy(ontologyTermSynonymRepository.iterator());
		populateRepositoy(ontologyTermNodePathRepository.iterator());
	}

	@Test
	public void ontologyTermRepositoryIterator() throws OWLOntologyCreationException
	{
		DataService dataService = mock(DataService.class);

		Entity entityOntology = new MapEntity();
		entityOntology.set(OntologyMetaData.ID, "13");
		entityOntology.set(OntologyMetaData.ONTOLOGY_IRI, "http://www.molgenis.org");
		entityOntology.set(OntologyMetaData.ONTOLOGY_NAME, "small_test_data");
		when(dataService.findOne(OntologyMetaData.ENTITY_NAME, new QueryImpl().eq(anyString(), anyString())))
				.thenReturn(entityOntology);

		// Entities
		MapEntity entity1 = new MapEntity();
		MapEntity entity2 = new MapEntity();

		// Synonym
		when(
				dataService.findAll(OntologyTermSynonymMetaData.ENTITY_NAME,
						new QueryImpl().in(OntologyMetaData.ID, Arrays.<String> asList("3")))).thenReturn(
				Arrays.asList(entity1));

		when(
				dataService.findAll(OntologyTermSynonymMetaData.ENTITY_NAME,
						new QueryImpl().in(OntologyMetaData.ID, Arrays.<String> asList("4")))).thenReturn(
				Arrays.asList(entity1));

		when(
				dataService.findAll(OntologyTermSynonymMetaData.ENTITY_NAME,
						new QueryImpl().in(OntologyMetaData.ID, Arrays.<String> asList("5")))).thenReturn(
				Arrays.asList(entity1));

		when(
				dataService.findAll(OntologyTermSynonymMetaData.ENTITY_NAME,
						new QueryImpl().in(OntologyMetaData.ID, Arrays.<String> asList()))).thenReturn(Arrays.asList());

		when(
				dataService.findAll(OntologyTermSynonymMetaData.ENTITY_NAME,
						new QueryImpl().in(OntologyMetaData.ID, Arrays.<String> asList("6")))).thenReturn(
				Arrays.asList(entity1));

		// DynamicAnnotation
		when(
				dataService.findAll(OntologyTermDynamicAnnotationMetaData.ENTITY_NAME,
						new QueryImpl().in(OntologyTermDynamicAnnotationMetaData.ID, Arrays.<String> asList())))
				.thenReturn(Arrays.asList());

		when(
				dataService.findAll(OntologyTermDynamicAnnotationMetaData.ENTITY_NAME,
						new QueryImpl().in(OntologyTermDynamicAnnotationMetaData.ID, Arrays.<String> asList("-1", "0"))))
				.thenReturn(Arrays.asList(entity1, entity2));

		when(
				dataService.findAll(OntologyTermDynamicAnnotationMetaData.ENTITY_NAME,
						new QueryImpl().in(OntologyTermDynamicAnnotationMetaData.ID, Arrays.<String> asList("1", "2"))))
				.thenReturn(Arrays.asList(entity1, entity2));

		when(
				dataService.findAll(OntologyTermDynamicAnnotationMetaData.ENTITY_NAME,
						new QueryImpl().in(OntologyTermDynamicAnnotationMetaData.ID, Arrays.<String> asList())))
				.thenReturn(Arrays.asList());

		// NodePath
		when(
				dataService.findAll(OntologyTermNodePathMetaData.ENTITY_NAME,
						new QueryImpl().in(OntologyTermDynamicAnnotationMetaData.ID, Arrays.<String> asList("8"))))
				.thenReturn(Arrays.asList(entity1));

		when(
				dataService.findAll(OntologyTermNodePathMetaData.ENTITY_NAME,
						new QueryImpl().in(OntologyTermDynamicAnnotationMetaData.ID, Arrays.<String> asList("11"))))
				.thenReturn(Arrays.asList(entity1));

		when(
				dataService.findAll(OntologyTermNodePathMetaData.ENTITY_NAME, new QueryImpl().in(
						OntologyTermDynamicAnnotationMetaData.ID, Arrays.<String> asList("10", "12")))).thenReturn(
				Arrays.asList(entity1, entity2));

		when(
				dataService.findAll(OntologyTermNodePathMetaData.ENTITY_NAME,
						new QueryImpl().in(OntologyTermDynamicAnnotationMetaData.ID, Arrays.<String> asList("7"))))
				.thenReturn(Arrays.asList(entity1));

		when(
				dataService.findAll(OntologyTermNodePathMetaData.ENTITY_NAME,
						new QueryImpl().in(OntologyTermDynamicAnnotationMetaData.ID, Arrays.<String> asList("9"))))
				.thenReturn(Arrays.asList(entity1));

		ontologyTermRepository = new OntologyTermRepository(ontologyLoader, uuidGenerator,
				ontologyTermDynamicAnnotationRepository, ontologyTermSynonymRepository, ontologyTermNodePathRepository,
				dataService);

		Iterator<Entity> i = ontologyTermRepository.iterator();
		assertTrue(i.hasNext());
		Entity entity = i.next();
		assertNotNull(entity.get(OntologyTermMetaData.ID));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_IRI), "http://www.molgenis.org#Organization");
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_NAME), "organization");
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM), Arrays.asList(entity1));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_DYNAMIC_ANNOTATION), Arrays.asList());
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_NODE_PATH), Arrays.asList(entity1));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY), entityOntology);

		entity = i.next();
		assertNotNull(entity.get(OntologyTermMetaData.ID));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_IRI), "http://www.molgenis.org#Team");
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_NAME), "team");
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM), Arrays.asList(entity1));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_DYNAMIC_ANNOTATION), Arrays.asList(entity1, entity2));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_NODE_PATH), Arrays.asList(entity1));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY), entityOntology);

		entity = i.next();
		assertNotNull(entity.get(OntologyTermMetaData.ID));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_IRI), "http://www.molgenis.org#GCC");
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_NAME), "Genomics coordination center");
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM), Arrays.asList(entity1));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_DYNAMIC_ANNOTATION), Arrays.asList(entity1, entity2));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_NODE_PATH), Arrays.asList(entity1, entity2));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY), entityOntology);

		entity = i.next();
		assertNotNull(entity.get(OntologyTermMetaData.ID));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_IRI), "top");
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_NAME), "top");
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM), Arrays.asList());
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_DYNAMIC_ANNOTATION), Arrays.asList());
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_NODE_PATH), Arrays.asList(entity1));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY), entityOntology);

		entity = i.next();
		assertNotNull(entity.get(OntologyTermMetaData.ID));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_IRI), "http://www.molgenis.org#hospital");
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_NAME), "hospital");
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM), Arrays.asList(entity1));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_DYNAMIC_ANNOTATION), Arrays.asList());
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_NODE_PATH), Arrays.asList(entity1));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY), entityOntology);

		assertFalse(i.hasNext());
	}

	private static void populateRepositoy(Iterator<Entity> iterator)
	{
		while (iterator.hasNext())
			iterator.next();
	}

	static class UuidGeneratorTest extends UuidGenerator
	{
		private int autoId = -1;

		@Override
		public String generateId()
		{
			return String.valueOf(autoId++);
		}
	}
}
