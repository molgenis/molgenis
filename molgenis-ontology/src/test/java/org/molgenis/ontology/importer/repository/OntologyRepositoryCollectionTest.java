package org.molgenis.ontology.importer.repository;

import static com.google.common.collect.Streams.stream;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.ontology.core.meta.OntologyMetadata.ONTOLOGY;
import static org.molgenis.ontology.core.meta.OntologyMetadata.ONTOLOGY_IRI;
import static org.molgenis.ontology.core.meta.OntologyMetadata.ONTOLOGY_NAME;
import static org.molgenis.ontology.core.meta.OntologyTermDynamicAnnotationMetadata.LABEL;
import static org.molgenis.ontology.core.meta.OntologyTermDynamicAnnotationMetadata.NAME;
import static org.molgenis.ontology.core.meta.OntologyTermDynamicAnnotationMetadata.ONTOLOGY_TERM_DYNAMIC_ANNOTATION;
import static org.molgenis.ontology.core.meta.OntologyTermDynamicAnnotationMetadata.VALUE;
import static org.molgenis.ontology.core.meta.OntologyTermMetadata.ONTOLOGY_TERM;
import static org.molgenis.ontology.core.meta.OntologyTermMetadata.ONTOLOGY_TERM_IRI;
import static org.molgenis.ontology.core.meta.OntologyTermMetadata.ONTOLOGY_TERM_NAME;
import static org.molgenis.ontology.core.meta.OntologyTermNodePathMetadata.NODE_PATH;
import static org.molgenis.ontology.core.meta.OntologyTermNodePathMetadata.ONTOLOGY_TERM_NODE_PATH;
import static org.molgenis.ontology.core.meta.OntologyTermSynonymMetadata.ONTOLOGY_TERM_SYNONYM;
import static org.molgenis.ontology.core.meta.OntologyTermSynonymMetadata.ONTOLOGY_TERM_SYNONYM_ATTR;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.ontology.core.config.OntologyTestConfig;
import org.molgenis.ontology.core.importer.repository.OntologyRepositoryCollection;
import org.molgenis.ontology.core.meta.OntologyTermDynamicAnnotationMetadata;
import org.molgenis.ontology.core.meta.OntologyTermMetadata;
import org.molgenis.ontology.core.meta.OntologyTermNodePathMetadata;
import org.molgenis.ontology.core.meta.OntologyTermSynonymMetadata;
import org.molgenis.util.ResourceUtils;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {OntologyRepositoryCollectionTest.Config.class})
class OntologyRepositoryCollectionTest extends AbstractMolgenisSpringTest {
  @Autowired private AutowireCapableBeanFactory autowireCapableBeanFactory;

  private Repository<Entity> ontologyRepository;
  private Repository<Entity> ontologyTermDynamicAnnotationRepository;
  private Repository<Entity> ontologyTermNodePathRepository;
  private Repository<Entity> ontologyTermSynonymRepository;
  private Repository<Entity> ontologyTermRepository;

  @BeforeEach
  void beforeMethod() throws IOException, OWLOntologyCreationException, NoSuchMethodException {

    // ontology repository collection is not spring managed, see FileRepositoryCollectionFactory
    File file = ResourceUtils.getFile("small_test_data_NGtest.owl.zip");
    OntologyRepositoryCollection ontologyRepoCollection =
        BeanUtils.instantiateClass(
            OntologyRepositoryCollection.class.getConstructor(File.class), file);
    autowireCapableBeanFactory.autowireBeanProperties(
        ontologyRepoCollection, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false);
    ontologyRepoCollection.init();

    ontologyRepository = ontologyRepoCollection.getRepository(ONTOLOGY);
    ontologyTermDynamicAnnotationRepository =
        ontologyRepoCollection.getRepository(ONTOLOGY_TERM_DYNAMIC_ANNOTATION);
    ontologyTermNodePathRepository = ontologyRepoCollection.getRepository(ONTOLOGY_TERM_NODE_PATH);
    ontologyTermSynonymRepository = ontologyRepoCollection.getRepository(ONTOLOGY_TERM_SYNONYM);
    ontologyTermRepository = ontologyRepoCollection.getRepository(ONTOLOGY_TERM);
  }

  @Test
  void ontologyRepositoryIterator() throws OWLOntologyCreationException {
    Iterator<Entity> i = ontologyRepository.iterator();
    assertTrue(i.hasNext());
    Entity entity = i.next();
    assertEquals("http://www.molgenis.org", entity.get(ONTOLOGY_IRI));
    assertEquals("small_test_data_NGtest", entity.get(ONTOLOGY_NAME));
    assertFalse(i.hasNext());
  }

  @Test
  void ontologyTermDynamicAnnotationRepositoryIterator() throws OWLOntologyCreationException {

    Iterator<Entity> i = ontologyTermDynamicAnnotationRepository.iterator();
    assertTrue(i.hasNext());
    Entity entity = i.next();
    assertNotNull(entity.get(OntologyTermDynamicAnnotationMetadata.ID));
    assertEquals("friday", entity.get(NAME));
    assertEquals("2412423", entity.get(VALUE));
    assertEquals("friday:2412423", entity.get(LABEL));

    assertTrue(i.hasNext());
    entity = i.next();
    assertNotNull(entity.get(OntologyTermDynamicAnnotationMetadata.ID));
    assertEquals("molgenis", entity.get(NAME));
    assertEquals("1231424", entity.get(VALUE));
    assertEquals("molgenis:1231424", entity.get(LABEL));

    assertTrue(i.hasNext());
    entity = i.next();
    assertNotNull(entity.get(OntologyTermDynamicAnnotationMetadata.ID));
    assertEquals("GCC", entity.get(NAME));
    assertEquals("987654", entity.get(VALUE));
    assertEquals("GCC:987654", entity.get(LABEL));

    assertTrue(i.hasNext());
    entity = i.next();
    assertNotNull(entity.get(OntologyTermDynamicAnnotationMetadata.ID));
    assertEquals("GCC", entity.get(NAME));
    assertEquals("123456", entity.get(VALUE));
    assertEquals("GCC:123456", entity.get(LABEL));

    assertFalse(i.hasNext());
  }

  @Test
  void ontologyTermNodePathRepositoryIterator() throws OWLOntologyCreationException {
    Iterator<Entity> i = ontologyTermNodePathRepository.iterator();
    assertTrue(i.hasNext());
    Entity entity = i.next();
    assertNotNull(entity.get(OntologyTermNodePathMetadata.ID));
    assertEquals("0[0]", entity.get(NODE_PATH));
    assertTrue(entity.getBoolean(OntologyTermNodePathMetadata.ROOT));

    entity = i.next();
    assertNotNull(entity.get(OntologyTermNodePathMetadata.ID));
    assertEquals("0[0].0[1]", entity.get(NODE_PATH));
    assertFalse(entity.getBoolean(OntologyTermNodePathMetadata.ROOT));

    entity = i.next();
    assertNotNull(entity.get(OntologyTermNodePathMetadata.ID));
    assertEquals("0[0].0[1].0[2]", entity.get(NODE_PATH));
    assertFalse(entity.getBoolean(OntologyTermNodePathMetadata.ROOT));

    entity = i.next();
    assertNotNull(entity.get(OntologyTermNodePathMetadata.ID));
    assertEquals("0[0].0[1].0[2].0[3]", entity.get(NODE_PATH));
    assertFalse(entity.getBoolean(OntologyTermNodePathMetadata.ROOT));

    entity = i.next();
    assertNotNull(entity.get(OntologyTermNodePathMetadata.ID));
    assertEquals("0[0].1[1]", entity.get(NODE_PATH));
    assertFalse(entity.getBoolean(OntologyTermNodePathMetadata.ROOT));

    entity = i.next();
    assertNotNull(entity.get(OntologyTermNodePathMetadata.ID));
    assertEquals("0[0].1[1].0[2]", entity.get(NODE_PATH));
    assertFalse(entity.getBoolean(OntologyTermNodePathMetadata.ROOT));

    entity = i.next();
    assertNotNull(entity.get(OntologyTermNodePathMetadata.ID));
    assertEquals("0[0].2[1]", entity.get(NODE_PATH));
    assertFalse(entity.getBoolean(OntologyTermNodePathMetadata.ROOT));

    entity = i.next();
    assertNotNull(entity.get(OntologyTermNodePathMetadata.ID));
    assertEquals("0[0].2[1].0[2]", entity.get(NODE_PATH));
    assertFalse(entity.getBoolean(OntologyTermNodePathMetadata.ROOT));

    entity = i.next();
    assertNotNull(entity.get(OntologyTermNodePathMetadata.ID));
    assertEquals("0[0].2[1].1[2]", entity.get(NODE_PATH));
    assertFalse(entity.getBoolean(OntologyTermNodePathMetadata.ROOT));

    entity = i.next();
    assertNotNull(entity.get(OntologyTermNodePathMetadata.ID));
    assertEquals("0[0].2[1].1[2].0[3]", entity.get(NODE_PATH));
    assertFalse(entity.getBoolean(OntologyTermNodePathMetadata.ROOT));

    assertFalse(i.hasNext());
  }

  @Test
  void ontologyTermSynonymRepositoryIterator() throws OWLOntologyCreationException {

    Iterator<Entity> i = ontologyTermSynonymRepository.iterator();
    assertTrue(i.hasNext());
    Entity entity = i.next();
    assertNotNull(entity.get(OntologyTermSynonymMetadata.ID));
    assertEquals("organization", entity.get(ONTOLOGY_TERM_SYNONYM_ATTR));

    assertTrue(i.hasNext());
    entity = i.next();
    assertNotNull(entity.get(OntologyTermSynonymMetadata.ID));
    assertEquals("team", entity.get(ONTOLOGY_TERM_SYNONYM_ATTR));

    assertTrue(i.hasNext());
    entity = i.next();
    assertNotNull(entity.get(OntologyTermSynonymMetadata.ID));
    assertEquals("Genomics coordination center", entity.get(ONTOLOGY_TERM_SYNONYM_ATTR));

    assertTrue(i.hasNext());
    entity = i.next();
    assertNotNull(entity.get(OntologyTermSynonymMetadata.ID));
    assertEquals("weight", entity.get(ONTOLOGY_TERM_SYNONYM_ATTR));

    assertTrue(i.hasNext());
    entity = i.next();
    assertNotNull(entity.get(OntologyTermSynonymMetadata.ID));
    assertEquals("top", entity.get(ONTOLOGY_TERM_SYNONYM_ATTR));

    assertTrue(i.hasNext());
    entity = i.next();
    assertNotNull(entity.get(OntologyTermSynonymMetadata.ID));
    assertEquals("measurement", entity.get(ONTOLOGY_TERM_SYNONYM_ATTR));

    assertTrue(i.hasNext());
    entity = i.next();
    assertNotNull(entity.get(OntologyTermSynonymMetadata.ID));
    assertEquals("body length", entity.get(ONTOLOGY_TERM_SYNONYM_ATTR));

    assertTrue(i.hasNext());
    entity = i.next();
    assertNotNull(entity.get(OntologyTermSynonymMetadata.ID));
    assertEquals("height", entity.get(ONTOLOGY_TERM_SYNONYM_ATTR));

    assertTrue(i.hasNext());
    entity = i.next();
    assertNotNull(entity.get(OntologyTermSynonymMetadata.ID));
    assertEquals("hospital", entity.get(ONTOLOGY_TERM_SYNONYM_ATTR));

    assertFalse(i.hasNext());
  }

  @Test
  void ontologyTermRepositoryIterator() throws OWLOntologyCreationException {
    Entity entityOntology = ontologyRepository.iterator().next();

    Iterator<Entity> i = ontologyTermRepository.iterator();
    assertTrue(i.hasNext());

    // Organization
    Entity entity = i.next();
    assertNotNull(entity.get(OntologyTermMetadata.ID));
    assertEquals("http://www.molgenis.org#Organization", entity.get(ONTOLOGY_TERM_IRI));
    assertEquals("organization", entity.get(ONTOLOGY_TERM_NAME));
    assertEquals(
        singletonList("organization"),
        getMrefAttributeList(
            entity, OntologyTermMetadata.ONTOLOGY_TERM_SYNONYM, ONTOLOGY_TERM_SYNONYM_ATTR));
    assertEquals(
        emptyList(),
        getMrefAttributeList(entity, OntologyTermMetadata.ONTOLOGY_TERM_DYNAMIC_ANNOTATION, LABEL));
    assertEquals(
        singletonList("0[0].0[1]"),
        getMrefAttributeList(entity, OntologyTermMetadata.ONTOLOGY_TERM_NODE_PATH, NODE_PATH));
    assertEquals(entityOntology, entity.get(OntologyTermMetadata.ONTOLOGY));

    // Team
    entity = i.next();
    assertNotNull(entity.get(OntologyTermMetadata.ID));
    assertEquals("http://www.molgenis.org#Team", entity.get(ONTOLOGY_TERM_IRI));
    assertEquals("team", entity.get(ONTOLOGY_TERM_NAME));
    assertEquals(
        singletonList("team"),
        getMrefAttributeList(
            entity, OntologyTermMetadata.ONTOLOGY_TERM_SYNONYM, ONTOLOGY_TERM_SYNONYM_ATTR));
    assertEquals(
        asList("friday:2412423", "molgenis:1231424"),
        getMrefAttributeList(entity, OntologyTermMetadata.ONTOLOGY_TERM_DYNAMIC_ANNOTATION, LABEL));
    assertEquals(
        singletonList("0[0].1[1]"),
        getMrefAttributeList(entity, OntologyTermMetadata.ONTOLOGY_TERM_NODE_PATH, NODE_PATH));
    assertEquals(entityOntology, entity.get(OntologyTermMetadata.ONTOLOGY));

    // GCC
    entity = i.next();
    assertNotNull(entity.get(OntologyTermMetadata.ID));
    assertEquals("http://www.molgenis.org#GCC", entity.get(ONTOLOGY_TERM_IRI));
    assertEquals("Genomics coordination center", entity.get(ONTOLOGY_TERM_NAME));
    assertEquals(
        singletonList("Genomics coordination center"),
        getMrefAttributeList(
            entity, OntologyTermMetadata.ONTOLOGY_TERM_SYNONYM, ONTOLOGY_TERM_SYNONYM_ATTR));
    assertEquals(
        asList("GCC:987654", "GCC:123456"),
        getMrefAttributeList(entity, OntologyTermMetadata.ONTOLOGY_TERM_DYNAMIC_ANNOTATION, LABEL));
    assertEquals(
        asList("0[0].0[1].0[2].0[3]", "0[0].1[1].0[2]"),
        getMrefAttributeList(entity, OntologyTermMetadata.ONTOLOGY_TERM_NODE_PATH, NODE_PATH));
    assertEquals(entityOntology, entity.get(OntologyTermMetadata.ONTOLOGY));

    // Weight
    entity = i.next();
    assertNotNull(entity.get(OntologyTermMetadata.ID));
    assertEquals("http://www.molgenis.org#weight", entity.get(ONTOLOGY_TERM_IRI));
    assertEquals("weight", entity.get(ONTOLOGY_TERM_NAME));
    assertEquals(
        singletonList("weight"),
        getMrefAttributeList(
            entity, OntologyTermMetadata.ONTOLOGY_TERM_SYNONYM, ONTOLOGY_TERM_SYNONYM_ATTR));
    assertEquals(
        emptyList(),
        getMrefAttributeList(entity, OntologyTermMetadata.ONTOLOGY_TERM_DYNAMIC_ANNOTATION, LABEL));
    assertEquals(
        singletonList("0[0].2[1].0[2]"),
        getMrefAttributeList(entity, OntologyTermMetadata.ONTOLOGY_TERM_NODE_PATH, NODE_PATH));
    assertEquals(entityOntology, entity.get(OntologyTermMetadata.ONTOLOGY));

    // Top
    entity = i.next();
    assertNotNull(entity.get(OntologyTermMetadata.ID));
    assertEquals("top", entity.get(ONTOLOGY_TERM_IRI));
    assertEquals("top", entity.get(ONTOLOGY_TERM_NAME));
    assertEquals(
        singletonList("top"),
        getMrefAttributeList(
            entity, OntologyTermMetadata.ONTOLOGY_TERM_SYNONYM, ONTOLOGY_TERM_SYNONYM_ATTR));
    assertEquals(
        emptyList(),
        getMrefAttributeList(entity, OntologyTermMetadata.ONTOLOGY_TERM_DYNAMIC_ANNOTATION, LABEL));
    assertEquals(
        singletonList("0[0]"),
        getMrefAttributeList(entity, OntologyTermMetadata.ONTOLOGY_TERM_NODE_PATH, NODE_PATH));
    assertEquals(entityOntology, entity.get(OntologyTermMetadata.ONTOLOGY));

    // Measurement
    entity = i.next();
    assertNotNull(entity.get(OntologyTermMetadata.ID));
    assertEquals("http://www.molgenis.org#measurement", entity.get(ONTOLOGY_TERM_IRI));
    assertEquals("measurement", entity.get(ONTOLOGY_TERM_NAME));
    assertEquals(
        singletonList("measurement"),
        getMrefAttributeList(
            entity, OntologyTermMetadata.ONTOLOGY_TERM_SYNONYM, ONTOLOGY_TERM_SYNONYM_ATTR));
    assertEquals(
        emptyList(),
        getMrefAttributeList(entity, OntologyTermMetadata.ONTOLOGY_TERM_DYNAMIC_ANNOTATION, LABEL));
    assertEquals(
        singletonList("0[0].2[1]"),
        getMrefAttributeList(entity, OntologyTermMetadata.ONTOLOGY_TERM_NODE_PATH, NODE_PATH));
    assertEquals(entityOntology, entity.get(OntologyTermMetadata.ONTOLOGY));

    // Body length
    entity = i.next();
    assertNotNull(entity.get(OntologyTermMetadata.ID));
    assertEquals("http://www.molgenis.org#body_length", entity.get(ONTOLOGY_TERM_IRI));
    assertEquals("body length", entity.get(ONTOLOGY_TERM_NAME));
    assertEquals(
        singletonList("body length"),
        getMrefAttributeList(
            entity, OntologyTermMetadata.ONTOLOGY_TERM_SYNONYM, ONTOLOGY_TERM_SYNONYM_ATTR));
    assertEquals(
        emptyList(),
        getMrefAttributeList(entity, OntologyTermMetadata.ONTOLOGY_TERM_DYNAMIC_ANNOTATION, LABEL));
    assertEquals(
        singletonList("0[0].2[1].1[2].0[3]"),
        getMrefAttributeList(entity, OntologyTermMetadata.ONTOLOGY_TERM_NODE_PATH, NODE_PATH));
    assertEquals(entityOntology, entity.get(OntologyTermMetadata.ONTOLOGY));

    // Height
    entity = i.next();
    assertNotNull(entity.get(OntologyTermMetadata.ID));
    assertEquals("http://www.molgenis.org#height", entity.get(ONTOLOGY_TERM_IRI));
    assertEquals("height", entity.get(ONTOLOGY_TERM_NAME));
    assertEquals(
        singletonList("height"),
        getMrefAttributeList(
            entity, OntologyTermMetadata.ONTOLOGY_TERM_SYNONYM, ONTOLOGY_TERM_SYNONYM_ATTR));
    assertEquals(
        emptyList(),
        getMrefAttributeList(entity, OntologyTermMetadata.ONTOLOGY_TERM_DYNAMIC_ANNOTATION, LABEL));
    assertEquals(
        singletonList("0[0].2[1].1[2]"),
        getMrefAttributeList(entity, OntologyTermMetadata.ONTOLOGY_TERM_NODE_PATH, NODE_PATH));
    assertEquals(entityOntology, entity.get(OntologyTermMetadata.ONTOLOGY));

    // Hospital
    entity = i.next();
    assertNotNull(entity.get(OntologyTermMetadata.ID));
    assertEquals("http://www.molgenis.org#hospital", entity.get(ONTOLOGY_TERM_IRI));
    assertEquals("hospital", entity.get(ONTOLOGY_TERM_NAME));
    assertEquals(
        singletonList("hospital"),
        getMrefAttributeList(
            entity, OntologyTermMetadata.ONTOLOGY_TERM_SYNONYM, ONTOLOGY_TERM_SYNONYM_ATTR));
    assertEquals(
        emptyList(),
        getMrefAttributeList(entity, OntologyTermMetadata.ONTOLOGY_TERM_DYNAMIC_ANNOTATION, LABEL));
    assertEquals(
        singletonList("0[0].0[1].0[2]"),
        getMrefAttributeList(entity, OntologyTermMetadata.ONTOLOGY_TERM_NODE_PATH, NODE_PATH));
    assertEquals(entityOntology, entity.get(OntologyTermMetadata.ONTOLOGY));

    assertFalse(i.hasNext());
  }

  private static List<String> getMrefAttributeList(
      Entity entity, String attributeName, String refEntityAttributeName) {
    return stream(entity.getEntities(attributeName))
        .map(e -> e.getString(refEntityAttributeName))
        .collect(Collectors.toList());
  }

  @Configuration
  @Import(OntologyTestConfig.class)
  static class Config {
    @Bean
    IdGenerator idGenerator() {
      IdGenerator idGenerator = mock(IdGenerator.class);
      when(idGenerator.generateId()).thenAnswer((invocation) -> UUID.randomUUID().toString());
      return idGenerator;
    }
  }
}
