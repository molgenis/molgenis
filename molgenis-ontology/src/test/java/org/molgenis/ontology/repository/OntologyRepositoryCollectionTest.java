package org.molgenis.ontology.repository;

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
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.ontology.core.meta.OntologyMetaData;
import org.molgenis.ontology.core.meta.OntologyTermDynamicAnnotationMetaData;
import org.molgenis.ontology.core.meta.OntologyTermMetaData;
import org.molgenis.ontology.core.meta.OntologyTermNodePathMetaData;
import org.molgenis.ontology.core.meta.OntologyTermSynonymMetaData;
import org.molgenis.ontology.importer.repository.OntologyRepositoryCollection;
import org.molgenis.util.ResourceUtils;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class OntologyRepositoryCollectionTest
{
	private static Repository ontologyRepository;
	private static Repository ontologyTermDynamicAnnotationRepository;
	private static Repository ontologyTermNodePathRepository;
	private static Repository ontologyTermSynonymRepository;
	private static Repository ontologyTermRepository;

	@BeforeClass
	public static void beforeClass() throws FileNotFoundException, IOException, OWLOntologyCreationException
	{
		File file = ResourceUtils.getFile("small_test_data.owl.zip");
		OntologyRepositoryCollection collection = new OntologyRepositoryCollection(file);
		ontologyRepository = collection.getRepository(OntologyMetaData.ENTITY_NAME);
		ontologyTermDynamicAnnotationRepository = collection
				.getRepository(OntologyTermDynamicAnnotationMetaData.ENTITY_NAME);
		ontologyTermNodePathRepository = collection.getRepository(OntologyTermNodePathMetaData.ENTITY_NAME);
		ontologyTermSynonymRepository = collection.getRepository(OntologyTermSynonymMetaData.ENTITY_NAME);
		ontologyTermRepository = collection.getRepository(OntologyTermMetaData.ENTITY_NAME);
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

	@Test
	public void ontologyTermNodePathRepositoryIterator() throws OWLOntologyCreationException
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

	@Test
	public void ontologyTermSynonymRepositoryIterator() throws OWLOntologyCreationException
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
		assertEquals(entity.get(OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM), "top");

		assertTrue(i.hasNext());
		entity = i.next();
		assertNotNull(entity.get(OntologyTermSynonymMetaData.ID));
		assertEquals(entity.get(OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM), "hospital");

		assertFalse(i.hasNext());
	}

	@Test
	public void ontologyTermRepositoryIterator() throws OWLOntologyCreationException
	{
		Entity entityOntology = ontologyRepository.iterator().next();

		Iterator<Entity> i = ontologyTermRepository.iterator();
		assertTrue(i.hasNext());
		Entity entity = i.next();
		assertNotNull(entity.get(OntologyTermMetaData.ID));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_IRI), "http://www.molgenis.org#Organization");
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_NAME), "organization");
		assertEquals(
				getMREFAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM,
						OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM), Arrays.asList("organization"));
		assertEquals(
				getMREFAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_DYNAMIC_ANNOTATION,
						OntologyTermDynamicAnnotationMetaData.LABEL), Arrays.asList());
		assertEquals(
				getMREFAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_NODE_PATH,
						OntologyTermNodePathMetaData.ONTOLOGY_TERM_NODE_PATH), Arrays.asList("0[0].0[1]"));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY), entityOntology);

		entity = i.next();
		assertNotNull(entity.get(OntologyTermMetaData.ID));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_IRI), "http://www.molgenis.org#Team");
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_NAME), "team");
		assertEquals(
				getMREFAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM,
						OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM), Arrays.asList("team"));
		assertEquals(
				getMREFAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_DYNAMIC_ANNOTATION,
						OntologyTermDynamicAnnotationMetaData.LABEL), Arrays.asList("friday:2412423",
						"molgenis:1231424"));
		assertEquals(
				getMREFAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_NODE_PATH,
						OntologyTermNodePathMetaData.ONTOLOGY_TERM_NODE_PATH), Arrays.asList("0[0].1[1]"));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY), entityOntology);

		entity = i.next();
		assertNotNull(entity.get(OntologyTermMetaData.ID));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_IRI), "http://www.molgenis.org#GCC");
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_NAME), "Genomics coordination center");
		assertEquals(
				getMREFAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM,
						OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM),
				Arrays.asList("Genomics coordination center"));
		assertEquals(
				getMREFAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_DYNAMIC_ANNOTATION,
						OntologyTermDynamicAnnotationMetaData.LABEL), Arrays.asList("GCC:987654", "GCC:123456"));
		assertEquals(
				getMREFAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_NODE_PATH,
						OntologyTermNodePathMetaData.ONTOLOGY_TERM_NODE_PATH), Arrays.asList("0[0].0[1].0[2].0[3]",
						"0[0].1[1].0[2]"));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY), entityOntology);

		entity = i.next();
		assertNotNull(entity.get(OntologyTermMetaData.ID));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_IRI), "top");
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_NAME), "top");
		assertEquals(
				getMREFAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM,
						OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM), Arrays.asList("top"));
		assertEquals(
				getMREFAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_DYNAMIC_ANNOTATION,
						OntologyTermDynamicAnnotationMetaData.LABEL), Arrays.asList());
		assertEquals(
				getMREFAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_NODE_PATH,
						OntologyTermNodePathMetaData.ONTOLOGY_TERM_NODE_PATH), Arrays.asList("0[0]"));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY), entityOntology);

		entity = i.next();
		assertNotNull(entity.get(OntologyTermMetaData.ID));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_IRI), "http://www.molgenis.org#hospital");
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_NAME), "hospital");
		assertEquals(
				getMREFAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM,
						OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM), Arrays.asList("hospital"));
		assertEquals(
				getMREFAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_DYNAMIC_ANNOTATION,
						OntologyTermDynamicAnnotationMetaData.LABEL), Arrays.asList());
		assertEquals(
				getMREFAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_NODE_PATH,
						OntologyTermNodePathMetaData.ONTOLOGY_TERM_NODE_PATH), Arrays.asList("0[0].0[1].0[2]"));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY), entityOntology);

		assertFalse(i.hasNext());
	}

	private List<String> getMREFAttributeList(Entity entity, String attributeName, String refEntityAttributeName)
	{
		return StreamSupport.stream(entity.getEntities(attributeName).spliterator(), false)
				.map(e -> e.getString(refEntityAttributeName)).collect(Collectors.toList());
	}
}
