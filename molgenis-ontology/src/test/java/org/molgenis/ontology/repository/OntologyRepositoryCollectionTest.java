package org.molgenis.ontology.repository;

import static org.molgenis.ontology.core.meta.OntologyMetaData.ONTOLOGY;
import static org.molgenis.ontology.core.meta.OntologyTermDynamicAnnotationMetaData.ONTOLOGY_TERM_DYNAMIC_ANNOTATION;
import static org.molgenis.ontology.core.meta.OntologyTermMetaData.ONTOLOGY_TERM;
import static org.molgenis.ontology.core.meta.OntologyTermNodePathMetaData.ONTOLOGY_TERM_NODE_PATH;
import static org.molgenis.ontology.core.meta.OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM;
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
	private static Repository<Entity> ontologyRepository;
	private static Repository<Entity> ontologyTermDynamicAnnotationRepository;
	private static Repository<Entity> ontologyTermNodePathRepository;
	private static Repository<Entity> ontologyTermSynonymRepository;
	private static Repository<Entity> ontologyTermRepository;

	@BeforeClass
	public static void beforeClass() throws FileNotFoundException, IOException, OWLOntologyCreationException
	{
		File file = ResourceUtils.getFile("small_test_data_NGtest.owl.zip");
		OntologyRepositoryCollection collection = new OntologyRepositoryCollection(file);
		ontologyRepository = collection.getRepository(ONTOLOGY);
		ontologyTermDynamicAnnotationRepository = collection.getRepository(ONTOLOGY_TERM_DYNAMIC_ANNOTATION);
		ontologyTermNodePathRepository = collection.getRepository(ONTOLOGY_TERM_NODE_PATH);
		ontologyTermSynonymRepository = collection.getRepository(ONTOLOGY_TERM_SYNONYM);
		ontologyTermRepository = collection.getRepository(ONTOLOGY_TERM);
	}

	@Test
	public void ontologyRepositoryIterator() throws OWLOntologyCreationException
	{
		Iterator<Entity> i = ontologyRepository.iterator();
		assertTrue(i.hasNext());
		Entity entity = i.next();
		assertEquals(entity.get(OntologyMetaData.ONTOLOGY_IRI), "http://www.molgenis.org");
		assertEquals(entity.get(OntologyMetaData.ONTOLOGY_NAME), "small_test_data_NGtest");
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
		assertEquals(entity.get(OntologyTermNodePathMetaData.ONTOLOGY_TERM_NODE_PATH_ATTR), "0[0]");
		assertTrue(entity.getBoolean(OntologyTermNodePathMetaData.ROOT));

		entity = i.next();
		assertNotNull(entity.get(OntologyTermNodePathMetaData.ID));
		assertEquals(entity.get(OntologyTermNodePathMetaData.ONTOLOGY_TERM_NODE_PATH_ATTR), "0[0].0[1]");
		assertFalse(entity.getBoolean(OntologyTermNodePathMetaData.ROOT));

		entity = i.next();
		assertNotNull(entity.get(OntologyTermNodePathMetaData.ID));
		assertEquals(entity.get(OntologyTermNodePathMetaData.ONTOLOGY_TERM_NODE_PATH_ATTR), "0[0].0[1].0[2]");
		assertFalse(entity.getBoolean(OntologyTermNodePathMetaData.ROOT));

		entity = i.next();
		assertNotNull(entity.get(OntologyTermNodePathMetaData.ID));
		assertEquals(entity.get(OntologyTermNodePathMetaData.ONTOLOGY_TERM_NODE_PATH_ATTR), "0[0].0[1].0[2].0[3]");
		assertFalse(entity.getBoolean(OntologyTermNodePathMetaData.ROOT));

		entity = i.next();
		assertNotNull(entity.get(OntologyTermNodePathMetaData.ID));
		assertEquals(entity.get(OntologyTermNodePathMetaData.ONTOLOGY_TERM_NODE_PATH_ATTR), "0[0].1[1]");
		assertFalse(entity.getBoolean(OntologyTermNodePathMetaData.ROOT));

		entity = i.next();
		assertNotNull(entity.get(OntologyTermNodePathMetaData.ID));
		assertEquals(entity.get(OntologyTermNodePathMetaData.ONTOLOGY_TERM_NODE_PATH_ATTR), "0[0].1[1].0[2]");
		assertFalse(entity.getBoolean(OntologyTermNodePathMetaData.ROOT));

		entity = i.next();
		assertNotNull(entity.get(OntologyTermNodePathMetaData.ID));
		assertEquals(entity.get(OntologyTermNodePathMetaData.ONTOLOGY_TERM_NODE_PATH_ATTR), "0[0].2[1]");
		assertFalse(entity.getBoolean(OntologyTermNodePathMetaData.ROOT));

		entity = i.next();
		assertNotNull(entity.get(OntologyTermNodePathMetaData.ID));
		assertEquals(entity.get(OntologyTermNodePathMetaData.ONTOLOGY_TERM_NODE_PATH_ATTR), "0[0].2[1].0[2]");
		assertFalse(entity.getBoolean(OntologyTermNodePathMetaData.ROOT));

		entity = i.next();
		assertNotNull(entity.get(OntologyTermNodePathMetaData.ID));
		assertEquals(entity.get(OntologyTermNodePathMetaData.ONTOLOGY_TERM_NODE_PATH_ATTR), "0[0].2[1].1[2]");
		assertFalse(entity.getBoolean(OntologyTermNodePathMetaData.ROOT));

		entity = i.next();
		assertNotNull(entity.get(OntologyTermNodePathMetaData.ID));
		assertEquals(entity.get(OntologyTermNodePathMetaData.ONTOLOGY_TERM_NODE_PATH_ATTR), "0[0].2[1].1[2].0[3]");
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
		assertEquals(entity.get(OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM_ATTR), "organization");

		assertTrue(i.hasNext());
		entity = i.next();
		assertNotNull(entity.get(OntologyTermSynonymMetaData.ID));
		assertEquals(entity.get(OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM_ATTR), "team");

		assertTrue(i.hasNext());
		entity = i.next();
		assertNotNull(entity.get(OntologyTermSynonymMetaData.ID));
		assertEquals(entity.get(OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM_ATTR),
				"Genomics coordination center");

		assertTrue(i.hasNext());
		entity = i.next();
		assertNotNull(entity.get(OntologyTermSynonymMetaData.ID));
		assertEquals(entity.get(OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM_ATTR), "weight");

		assertTrue(i.hasNext());
		entity = i.next();
		assertNotNull(entity.get(OntologyTermSynonymMetaData.ID));
		assertEquals(entity.get(OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM_ATTR), "top");

		assertTrue(i.hasNext());
		entity = i.next();
		assertNotNull(entity.get(OntologyTermSynonymMetaData.ID));
		assertEquals(entity.get(OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM_ATTR), "measurement");

		assertTrue(i.hasNext());
		entity = i.next();
		assertNotNull(entity.get(OntologyTermSynonymMetaData.ID));
		assertEquals(entity.get(OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM_ATTR), "body length");

		assertTrue(i.hasNext());
		entity = i.next();
		assertNotNull(entity.get(OntologyTermSynonymMetaData.ID));
		assertEquals(entity.get(OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM_ATTR), "height");

		assertTrue(i.hasNext());
		entity = i.next();
		assertNotNull(entity.get(OntologyTermSynonymMetaData.ID));
		assertEquals(entity.get(OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM_ATTR), "hospital");

		assertFalse(i.hasNext());
	}

	@Test
	public void ontologyTermRepositoryIterator() throws OWLOntologyCreationException
	{
		Entity entityOntology = ontologyRepository.iterator().next();

		Iterator<Entity> i = ontologyTermRepository.iterator();
		assertTrue(i.hasNext());

		// Organization
		Entity entity = i.next();
		assertNotNull(entity.get(OntologyTermMetaData.ID));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_IRI), "http://www.molgenis.org#Organization");
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_NAME), "organization");
		assertEquals(
				getMREFAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM,
						OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM_ATTR), Arrays.asList("organization"));
		assertEquals(
				getMREFAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_DYNAMIC_ANNOTATION,
						OntologyTermDynamicAnnotationMetaData.LABEL), Arrays.asList());
		assertEquals(
				getMREFAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_NODE_PATH,
						OntologyTermNodePathMetaData.ONTOLOGY_TERM_NODE_PATH_ATTR), Arrays.asList("0[0].0[1]"));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY), entityOntology);

		// Team
		entity = i.next();
		assertNotNull(entity.get(OntologyTermMetaData.ID));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_IRI), "http://www.molgenis.org#Team");
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_NAME), "team");
		assertEquals(
				getMREFAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM,
						OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM_ATTR), Arrays.asList("team"));
		assertEquals(
				getMREFAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_DYNAMIC_ANNOTATION,
						OntologyTermDynamicAnnotationMetaData.LABEL), Arrays.asList("friday:2412423",
						"molgenis:1231424"));
		assertEquals(
				getMREFAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_NODE_PATH,
						OntologyTermNodePathMetaData.ONTOLOGY_TERM_NODE_PATH_ATTR), Arrays.asList("0[0].1[1]"));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY), entityOntology);

		// GCC
		entity = i.next();
		assertNotNull(entity.get(OntologyTermMetaData.ID));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_IRI), "http://www.molgenis.org#GCC");
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_NAME), "Genomics coordination center");
		assertEquals(
				getMREFAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM,
						OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM_ATTR),
				Arrays.asList("Genomics coordination center"));
		assertEquals(
				getMREFAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_DYNAMIC_ANNOTATION,
						OntologyTermDynamicAnnotationMetaData.LABEL), Arrays.asList("GCC:987654", "GCC:123456"));
		assertEquals(
				getMREFAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_NODE_PATH,
						OntologyTermNodePathMetaData.ONTOLOGY_TERM_NODE_PATH_ATTR), Arrays.asList("0[0].0[1].0[2].0[3]",
						"0[0].1[1].0[2]"));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY), entityOntology);

		// Weight
		entity = i.next();
		assertNotNull(entity.get(OntologyTermMetaData.ID));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_IRI), "http://www.molgenis.org#weight");
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_NAME), "weight");
		assertEquals(
				getMREFAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM,
						OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM_ATTR), Arrays.asList("weight"));
		assertEquals(
				getMREFAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_DYNAMIC_ANNOTATION,
						OntologyTermDynamicAnnotationMetaData.LABEL), Arrays.asList());
		assertEquals(
				getMREFAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_NODE_PATH,
						OntologyTermNodePathMetaData.ONTOLOGY_TERM_NODE_PATH_ATTR), Arrays.asList("0[0].2[1].0[2]"));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY), entityOntology);

		// Top
		entity = i.next();
		assertNotNull(entity.get(OntologyTermMetaData.ID));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_IRI), "top");
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_NAME), "top");
		assertEquals(
				getMREFAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM,
						OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM_ATTR), Arrays.asList("top"));
		assertEquals(
				getMREFAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_DYNAMIC_ANNOTATION,
						OntologyTermDynamicAnnotationMetaData.LABEL), Arrays.asList());
		assertEquals(
				getMREFAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_NODE_PATH,
						OntologyTermNodePathMetaData.ONTOLOGY_TERM_NODE_PATH_ATTR), Arrays.asList("0[0]"));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY), entityOntology);

		// Measurement
		entity = i.next();
		assertNotNull(entity.get(OntologyTermMetaData.ID));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_IRI), "http://www.molgenis.org#measurement");
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_NAME), "measurement");
		assertEquals(
				getMREFAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM,
						OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM_ATTR), Arrays.asList("measurement"));
		assertEquals(
				getMREFAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_DYNAMIC_ANNOTATION,
						OntologyTermDynamicAnnotationMetaData.LABEL), Arrays.asList());
		assertEquals(
				getMREFAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_NODE_PATH,
						OntologyTermNodePathMetaData.ONTOLOGY_TERM_NODE_PATH_ATTR), Arrays.asList("0[0].2[1]"));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY), entityOntology);

		// Body length
		entity = i.next();
		assertNotNull(entity.get(OntologyTermMetaData.ID));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_IRI), "http://www.molgenis.org#body_length");
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_NAME), "body length");
		assertEquals(
				getMREFAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM,
						OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM_ATTR), Arrays.asList("body length"));
		assertEquals(
				getMREFAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_DYNAMIC_ANNOTATION,
						OntologyTermDynamicAnnotationMetaData.LABEL), Arrays.asList());
		assertEquals(
				getMREFAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_NODE_PATH,
						OntologyTermNodePathMetaData.ONTOLOGY_TERM_NODE_PATH_ATTR),
				Arrays.asList("0[0].2[1].1[2].0[3]"));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY), entityOntology);

		// Height
		entity = i.next();
		assertNotNull(entity.get(OntologyTermMetaData.ID));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_IRI), "http://www.molgenis.org#height");
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_NAME), "height");
		assertEquals(
				getMREFAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM,
						OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM_ATTR), Arrays.asList("height"));
		assertEquals(
				getMREFAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_DYNAMIC_ANNOTATION,
						OntologyTermDynamicAnnotationMetaData.LABEL), Arrays.asList());
		assertEquals(
				getMREFAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_NODE_PATH,
						OntologyTermNodePathMetaData.ONTOLOGY_TERM_NODE_PATH_ATTR), Arrays.asList("0[0].2[1].1[2]"));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY), entityOntology);

		// Hospital
		entity = i.next();
		assertNotNull(entity.get(OntologyTermMetaData.ID));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_IRI), "http://www.molgenis.org#hospital");
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_NAME), "hospital");
		assertEquals(
				getMREFAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM,
						OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM_ATTR), Arrays.asList("hospital"));
		assertEquals(
				getMREFAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_DYNAMIC_ANNOTATION,
						OntologyTermDynamicAnnotationMetaData.LABEL), Arrays.asList());
		assertEquals(
				getMREFAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_NODE_PATH,
						OntologyTermNodePathMetaData.ONTOLOGY_TERM_NODE_PATH_ATTR), Arrays.asList("0[0].0[1].0[2]"));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY), entityOntology);

		assertFalse(i.hasNext());
	}

	private List<String> getMREFAttributeList(Entity entity, String attributeName, String refEntityAttributeName)
	{
		return StreamSupport.stream(entity.getEntities(attributeName).spliterator(), false)
				.map(e -> e.getString(refEntityAttributeName)).collect(Collectors.toList());
	}
}
