package org.molgenis.ontology.importer.repository;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.ontology.core.meta.*;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
import org.molgenis.util.ResourceUtils;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.ontology.core.meta.OntologyMetaData.ONTOLOGY;
import static org.molgenis.ontology.core.meta.OntologyTermDynamicAnnotationMetaData.ONTOLOGY_TERM_DYNAMIC_ANNOTATION;
import static org.molgenis.ontology.core.meta.OntologyTermMetaData.ONTOLOGY_TERM;
import static org.molgenis.ontology.core.meta.OntologyTermNodePathMetaData.ONTOLOGY_TERM_NODE_PATH;
import static org.molgenis.ontology.core.meta.OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM;
import static org.testng.Assert.*;

@ContextConfiguration(classes = { OntologyRepositoryCollectionTest.Config.class })
public class OntologyRepositoryCollectionTest extends AbstractMolgenisSpringTest
{
	@Autowired
	private AutowireCapableBeanFactory autowireCapableBeanFactory;

	private Repository<Entity> ontologyRepository;
	private Repository<Entity> ontologyTermDynamicAnnotationRepository;
	private Repository<Entity> ontologyTermNodePathRepository;
	private Repository<Entity> ontologyTermSynonymRepository;
	private Repository<Entity> ontologyTermRepository;

	@BeforeMethod
	public void beforeMethod() throws IOException, OWLOntologyCreationException, NoSuchMethodException
	{

		// ontology repository collection is not spring managed, see FileRepositoryCollectionFactory
		File file = ResourceUtils.getFile("small_test_data_NGtest.owl.zip");
		OntologyRepositoryCollection ontologyRepoCollection = BeanUtils
				.instantiateClass(OntologyRepositoryCollection.class.getConstructor(File.class), file);
		autowireCapableBeanFactory
				.autowireBeanProperties(ontologyRepoCollection, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false);
		ontologyRepoCollection.init();

		ontologyRepository = ontologyRepoCollection.getRepository(ONTOLOGY);
		ontologyTermDynamicAnnotationRepository = ontologyRepoCollection
				.getRepository(ONTOLOGY_TERM_DYNAMIC_ANNOTATION);
		ontologyTermNodePathRepository = ontologyRepoCollection.getRepository(ONTOLOGY_TERM_NODE_PATH);
		ontologyTermSynonymRepository = ontologyRepoCollection.getRepository(ONTOLOGY_TERM_SYNONYM);
		ontologyTermRepository = ontologyRepoCollection.getRepository(ONTOLOGY_TERM);
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
		assertEquals(entity.get(OntologyTermNodePathMetaData.NODE_PATH), "0[0]");
		assertTrue(entity.getBoolean(OntologyTermNodePathMetaData.ROOT));

		entity = i.next();
		assertNotNull(entity.get(OntologyTermNodePathMetaData.ID));
		assertEquals(entity.get(OntologyTermNodePathMetaData.NODE_PATH), "0[0].0[1]");
		assertFalse(entity.getBoolean(OntologyTermNodePathMetaData.ROOT));

		entity = i.next();
		assertNotNull(entity.get(OntologyTermNodePathMetaData.ID));
		assertEquals(entity.get(OntologyTermNodePathMetaData.NODE_PATH), "0[0].0[1].0[2]");
		assertFalse(entity.getBoolean(OntologyTermNodePathMetaData.ROOT));

		entity = i.next();
		assertNotNull(entity.get(OntologyTermNodePathMetaData.ID));
		assertEquals(entity.get(OntologyTermNodePathMetaData.NODE_PATH), "0[0].0[1].0[2].0[3]");
		assertFalse(entity.getBoolean(OntologyTermNodePathMetaData.ROOT));

		entity = i.next();
		assertNotNull(entity.get(OntologyTermNodePathMetaData.ID));
		assertEquals(entity.get(OntologyTermNodePathMetaData.NODE_PATH), "0[0].1[1]");
		assertFalse(entity.getBoolean(OntologyTermNodePathMetaData.ROOT));

		entity = i.next();
		assertNotNull(entity.get(OntologyTermNodePathMetaData.ID));
		assertEquals(entity.get(OntologyTermNodePathMetaData.NODE_PATH), "0[0].1[1].0[2]");
		assertFalse(entity.getBoolean(OntologyTermNodePathMetaData.ROOT));

		entity = i.next();
		assertNotNull(entity.get(OntologyTermNodePathMetaData.ID));
		assertEquals(entity.get(OntologyTermNodePathMetaData.NODE_PATH), "0[0].2[1]");
		assertFalse(entity.getBoolean(OntologyTermNodePathMetaData.ROOT));

		entity = i.next();
		assertNotNull(entity.get(OntologyTermNodePathMetaData.ID));
		assertEquals(entity.get(OntologyTermNodePathMetaData.NODE_PATH), "0[0].2[1].0[2]");
		assertFalse(entity.getBoolean(OntologyTermNodePathMetaData.ROOT));

		entity = i.next();
		assertNotNull(entity.get(OntologyTermNodePathMetaData.ID));
		assertEquals(entity.get(OntologyTermNodePathMetaData.NODE_PATH), "0[0].2[1].1[2]");
		assertFalse(entity.getBoolean(OntologyTermNodePathMetaData.ROOT));

		entity = i.next();
		assertNotNull(entity.get(OntologyTermNodePathMetaData.ID));
		assertEquals(entity.get(OntologyTermNodePathMetaData.NODE_PATH), "0[0].2[1].1[2].0[3]");
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
		assertEquals(getMrefAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM,
				OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM_ATTR), singletonList("organization"));
		assertEquals(getMrefAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_DYNAMIC_ANNOTATION,
				OntologyTermDynamicAnnotationMetaData.LABEL), emptyList());
		assertEquals(getMrefAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_NODE_PATH,
				OntologyTermNodePathMetaData.NODE_PATH), singletonList("0[0].0[1]"));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY), entityOntology);

		// Team
		entity = i.next();
		assertNotNull(entity.get(OntologyTermMetaData.ID));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_IRI), "http://www.molgenis.org#Team");
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_NAME), "team");
		assertEquals(getMrefAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM,
				OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM_ATTR), singletonList("team"));
		assertEquals(getMrefAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_DYNAMIC_ANNOTATION,
				OntologyTermDynamicAnnotationMetaData.LABEL), asList("friday:2412423", "molgenis:1231424"));
		assertEquals(getMrefAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_NODE_PATH,
				OntologyTermNodePathMetaData.NODE_PATH), singletonList("0[0].1[1]"));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY), entityOntology);

		// GCC
		entity = i.next();
		assertNotNull(entity.get(OntologyTermMetaData.ID));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_IRI), "http://www.molgenis.org#GCC");
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_NAME), "Genomics coordination center");
		assertEquals(getMrefAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM,
				OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM_ATTR), singletonList("Genomics coordination center"));
		assertEquals(getMrefAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_DYNAMIC_ANNOTATION,
				OntologyTermDynamicAnnotationMetaData.LABEL), asList("GCC:987654", "GCC:123456"));
		assertEquals(getMrefAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_NODE_PATH,
				OntologyTermNodePathMetaData.NODE_PATH), asList("0[0].0[1].0[2].0[3]", "0[0].1[1].0[2]"));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY), entityOntology);

		// Weight
		entity = i.next();
		assertNotNull(entity.get(OntologyTermMetaData.ID));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_IRI), "http://www.molgenis.org#weight");
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_NAME), "weight");
		assertEquals(getMrefAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM,
				OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM_ATTR), singletonList("weight"));
		assertEquals(getMrefAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_DYNAMIC_ANNOTATION,
				OntologyTermDynamicAnnotationMetaData.LABEL), emptyList());
		assertEquals(getMrefAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_NODE_PATH,
				OntologyTermNodePathMetaData.NODE_PATH), singletonList("0[0].2[1].0[2]"));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY), entityOntology);

		// Top
		entity = i.next();
		assertNotNull(entity.get(OntologyTermMetaData.ID));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_IRI), "top");
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_NAME), "top");
		assertEquals(getMrefAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM,
				OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM_ATTR), singletonList("top"));
		assertEquals(getMrefAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_DYNAMIC_ANNOTATION,
				OntologyTermDynamicAnnotationMetaData.LABEL), emptyList());
		assertEquals(getMrefAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_NODE_PATH,
				OntologyTermNodePathMetaData.NODE_PATH), singletonList("0[0]"));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY), entityOntology);

		// Measurement
		entity = i.next();
		assertNotNull(entity.get(OntologyTermMetaData.ID));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_IRI), "http://www.molgenis.org#measurement");
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_NAME), "measurement");
		assertEquals(getMrefAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM,
				OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM_ATTR), singletonList("measurement"));
		assertEquals(getMrefAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_DYNAMIC_ANNOTATION,
				OntologyTermDynamicAnnotationMetaData.LABEL), emptyList());
		assertEquals(getMrefAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_NODE_PATH,
				OntologyTermNodePathMetaData.NODE_PATH), singletonList("0[0].2[1]"));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY), entityOntology);

		// Body length
		entity = i.next();
		assertNotNull(entity.get(OntologyTermMetaData.ID));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_IRI), "http://www.molgenis.org#body_length");
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_NAME), "body length");
		assertEquals(getMrefAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM,
				OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM_ATTR), singletonList("body length"));
		assertEquals(getMrefAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_DYNAMIC_ANNOTATION,
				OntologyTermDynamicAnnotationMetaData.LABEL), emptyList());
		assertEquals(getMrefAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_NODE_PATH,
				OntologyTermNodePathMetaData.NODE_PATH), singletonList("0[0].2[1].1[2].0[3]"));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY), entityOntology);

		// Height
		entity = i.next();
		assertNotNull(entity.get(OntologyTermMetaData.ID));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_IRI), "http://www.molgenis.org#height");
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_NAME), "height");
		assertEquals(getMrefAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM,
				OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM_ATTR), singletonList("height"));
		assertEquals(getMrefAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_DYNAMIC_ANNOTATION,
				OntologyTermDynamicAnnotationMetaData.LABEL), emptyList());
		assertEquals(getMrefAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_NODE_PATH,
				OntologyTermNodePathMetaData.NODE_PATH), singletonList("0[0].2[1].1[2]"));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY), entityOntology);

		// Hospital
		entity = i.next();
		assertNotNull(entity.get(OntologyTermMetaData.ID));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_IRI), "http://www.molgenis.org#hospital");
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY_TERM_NAME), "hospital");
		assertEquals(getMrefAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_SYNONYM,
				OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM_ATTR), singletonList("hospital"));
		assertEquals(getMrefAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_DYNAMIC_ANNOTATION,
				OntologyTermDynamicAnnotationMetaData.LABEL), emptyList());
		assertEquals(getMrefAttributeList(entity, OntologyTermMetaData.ONTOLOGY_TERM_NODE_PATH,
				OntologyTermNodePathMetaData.NODE_PATH), singletonList("0[0].0[1].0[2]"));
		assertEquals(entity.get(OntologyTermMetaData.ONTOLOGY), entityOntology);

		assertFalse(i.hasNext());
	}

	private static List<String> getMrefAttributeList(Entity entity, String attributeName, String refEntityAttributeName)
	{
		return StreamSupport.stream(entity.getEntities(attributeName).spliterator(), false)
				.map(e -> e.getString(refEntityAttributeName)).collect(Collectors.toList());
	}

	@Configuration
	@ComponentScan({ "org.molgenis.ontology.core.meta", "org.molgenis.ontology.core.model" })
	public static class Config
	{
		@Bean
		public IdGenerator idGenerator()
		{
			IdGenerator idGenerator = mock(IdGenerator.class);
			when(idGenerator.generateId()).thenAnswer(new GenerateIdAnswer());
			return idGenerator;
		}

		private static class GenerateIdAnswer implements Answer<String>
		{
			@Override
			public String answer(InvocationOnMock invocation) throws Throwable
			{
				return String.valueOf(System.nanoTime());
			}
		}
	}
}
