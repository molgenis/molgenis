package org.molgenis.ontology.importer.repository;

import static com.google.common.collect.Streams.stream;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.ontology.core.meta.OntologyMetadata.ONTOLOGY;
import static org.molgenis.ontology.core.meta.OntologyTermDynamicAnnotationMetadata.ONTOLOGY_TERM_DYNAMIC_ANNOTATION;
import static org.molgenis.ontology.core.meta.OntologyTermMetadata.ONTOLOGY_TERM;
import static org.molgenis.ontology.core.meta.OntologyTermNodePathMetadata.ONTOLOGY_TERM_NODE_PATH;
import static org.molgenis.ontology.core.meta.OntologyTermSynonymMetadata.ONTOLOGY_TERM_SYNONYM;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.ontology.core.config.OntologyTestConfig;
import org.molgenis.ontology.core.importer.repository.OntologyRepositoryCollection;
import org.molgenis.ontology.core.meta.OntologyMetadata;
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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@ContextConfiguration(classes = {OntologyRepositoryCollectionTest.Config.class})
public class OntologyRepositoryCollectionTest extends AbstractMolgenisSpringTest {
  @Autowired private AutowireCapableBeanFactory autowireCapableBeanFactory;

  private Repository<Entity> ontologyRepository;
  private Repository<Entity> ontologyTermDynamicAnnotationRepository;
  private Repository<Entity> ontologyTermNodePathRepository;
  private Repository<Entity> ontologyTermSynonymRepository;
  private Repository<Entity> ontologyTermRepository;

  @BeforeMethod
  public void beforeMethod()
      throws IOException, OWLOntologyCreationException, NoSuchMethodException {

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
  public void ontologyRepositoryIterator() throws OWLOntologyCreationException {
    Iterator<Entity> i = ontologyRepository.iterator();
    assertTrue(i.hasNext());
    Entity entity = i.next();
    assertEquals(entity.get(OntologyMetadata.ONTOLOGY_IRI), "http://www.molgenis.org");
    assertEquals(entity.get(OntologyMetadata.ONTOLOGY_NAME), "small_test_data_NGtest");
    assertFalse(i.hasNext());
  }

  @Test
  public void ontologyTermDynamicAnnotationRepositoryIterator()
      throws OWLOntologyCreationException {

    Iterator<Entity> i = ontologyTermDynamicAnnotationRepository.iterator();
    assertTrue(i.hasNext());
    Entity entity = i.next();
    assertNotNull(entity.get(OntologyTermDynamicAnnotationMetadata.ID));
    assertEquals(entity.get(OntologyTermDynamicAnnotationMetadata.NAME), "friday");
    assertEquals(entity.get(OntologyTermDynamicAnnotationMetadata.VALUE), "2412423");
    assertEquals(entity.get(OntologyTermDynamicAnnotationMetadata.LABEL), "friday:2412423");

    assertTrue(i.hasNext());
    entity = i.next();
    assertNotNull(entity.get(OntologyTermDynamicAnnotationMetadata.ID));
    assertEquals(entity.get(OntologyTermDynamicAnnotationMetadata.NAME), "molgenis");
    assertEquals(entity.get(OntologyTermDynamicAnnotationMetadata.VALUE), "1231424");
    assertEquals(entity.get(OntologyTermDynamicAnnotationMetadata.LABEL), "molgenis:1231424");

    assertTrue(i.hasNext());
    entity = i.next();
    assertNotNull(entity.get(OntologyTermDynamicAnnotationMetadata.ID));
    assertEquals(entity.get(OntologyTermDynamicAnnotationMetadata.NAME), "GCC");
    assertEquals(entity.get(OntologyTermDynamicAnnotationMetadata.VALUE), "987654");
    assertEquals(entity.get(OntologyTermDynamicAnnotationMetadata.LABEL), "GCC:987654");

    assertTrue(i.hasNext());
    entity = i.next();
    assertNotNull(entity.get(OntologyTermDynamicAnnotationMetadata.ID));
    assertEquals(entity.get(OntologyTermDynamicAnnotationMetadata.NAME), "GCC");
    assertEquals(entity.get(OntologyTermDynamicAnnotationMetadata.VALUE), "123456");
    assertEquals(entity.get(OntologyTermDynamicAnnotationMetadata.LABEL), "GCC:123456");

    assertFalse(i.hasNext());
  }

  @Test
  public void ontologyTermNodePathRepositoryIterator() throws OWLOntologyCreationException {
    Iterator<Entity> i = ontologyTermNodePathRepository.iterator();
    assertTrue(i.hasNext());
    Entity entity = i.next();
    assertNotNull(entity.get(OntologyTermNodePathMetadata.ID));
    assertEquals(entity.get(OntologyTermNodePathMetadata.NODE_PATH), "0[0]");
    assertTrue(entity.getBoolean(OntologyTermNodePathMetadata.ROOT));

    entity = i.next();
    assertNotNull(entity.get(OntologyTermNodePathMetadata.ID));
    assertEquals(entity.get(OntologyTermNodePathMetadata.NODE_PATH), "0[0].0[1]");
    assertFalse(entity.getBoolean(OntologyTermNodePathMetadata.ROOT));

    entity = i.next();
    assertNotNull(entity.get(OntologyTermNodePathMetadata.ID));
    assertEquals(entity.get(OntologyTermNodePathMetadata.NODE_PATH), "0[0].0[1].0[2]");
    assertFalse(entity.getBoolean(OntologyTermNodePathMetadata.ROOT));

    entity = i.next();
    assertNotNull(entity.get(OntologyTermNodePathMetadata.ID));
    assertEquals(entity.get(OntologyTermNodePathMetadata.NODE_PATH), "0[0].0[1].0[2].0[3]");
    assertFalse(entity.getBoolean(OntologyTermNodePathMetadata.ROOT));

    entity = i.next();
    assertNotNull(entity.get(OntologyTermNodePathMetadata.ID));
    assertEquals(entity.get(OntologyTermNodePathMetadata.NODE_PATH), "0[0].1[1]");
    assertFalse(entity.getBoolean(OntologyTermNodePathMetadata.ROOT));

    entity = i.next();
    assertNotNull(entity.get(OntologyTermNodePathMetadata.ID));
    assertEquals(entity.get(OntologyTermNodePathMetadata.NODE_PATH), "0[0].1[1].0[2]");
    assertFalse(entity.getBoolean(OntologyTermNodePathMetadata.ROOT));

    entity = i.next();
    assertNotNull(entity.get(OntologyTermNodePathMetadata.ID));
    assertEquals(entity.get(OntologyTermNodePathMetadata.NODE_PATH), "0[0].2[1]");
    assertFalse(entity.getBoolean(OntologyTermNodePathMetadata.ROOT));

    entity = i.next();
    assertNotNull(entity.get(OntologyTermNodePathMetadata.ID));
    assertEquals(entity.get(OntologyTermNodePathMetadata.NODE_PATH), "0[0].2[1].0[2]");
    assertFalse(entity.getBoolean(OntologyTermNodePathMetadata.ROOT));

    entity = i.next();
    assertNotNull(entity.get(OntologyTermNodePathMetadata.ID));
    assertEquals(entity.get(OntologyTermNodePathMetadata.NODE_PATH), "0[0].2[1].1[2]");
    assertFalse(entity.getBoolean(OntologyTermNodePathMetadata.ROOT));

    entity = i.next();
    assertNotNull(entity.get(OntologyTermNodePathMetadata.ID));
    assertEquals(entity.get(OntologyTermNodePathMetadata.NODE_PATH), "0[0].2[1].1[2].0[3]");
    assertFalse(entity.getBoolean(OntologyTermNodePathMetadata.ROOT));

    assertFalse(i.hasNext());
  }

  @Test
  public void ontologyTermSynonymRepositoryIterator() throws OWLOntologyCreationException {

    Iterator<Entity> i = ontologyTermSynonymRepository.iterator();
    assertTrue(i.hasNext());
    Entity entity = i.next();
    assertNotNull(entity.get(OntologyTermSynonymMetadata.ID));
    assertEquals(
        entity.get(OntologyTermSynonymMetadata.ONTOLOGY_TERM_SYNONYM_ATTR), "organization");

    assertTrue(i.hasNext());
    entity = i.next();
    assertNotNull(entity.get(OntologyTermSynonymMetadata.ID));
    assertEquals(entity.get(OntologyTermSynonymMetadata.ONTOLOGY_TERM_SYNONYM_ATTR), "team");

    assertTrue(i.hasNext());
    entity = i.next();
    assertNotNull(entity.get(OntologyTermSynonymMetadata.ID));
    assertEquals(
        entity.get(OntologyTermSynonymMetadata.ONTOLOGY_TERM_SYNONYM_ATTR),
        "Genomics coordination center");

    assertTrue(i.hasNext());
    entity = i.next();
    assertNotNull(entity.get(OntologyTermSynonymMetadata.ID));
    assertEquals(entity.get(OntologyTermSynonymMetadata.ONTOLOGY_TERM_SYNONYM_ATTR), "weight");

    assertTrue(i.hasNext());
    entity = i.next();
    assertNotNull(entity.get(OntologyTermSynonymMetadata.ID));
    assertEquals(entity.get(OntologyTermSynonymMetadata.ONTOLOGY_TERM_SYNONYM_ATTR), "top");

    assertTrue(i.hasNext());
    entity = i.next();
    assertNotNull(entity.get(OntologyTermSynonymMetadata.ID));
    assertEquals(entity.get(OntologyTermSynonymMetadata.ONTOLOGY_TERM_SYNONYM_ATTR), "measurement");

    assertTrue(i.hasNext());
    entity = i.next();
    assertNotNull(entity.get(OntologyTermSynonymMetadata.ID));
    assertEquals(entity.get(OntologyTermSynonymMetadata.ONTOLOGY_TERM_SYNONYM_ATTR), "body length");

    assertTrue(i.hasNext());
    entity = i.next();
    assertNotNull(entity.get(OntologyTermSynonymMetadata.ID));
    assertEquals(entity.get(OntologyTermSynonymMetadata.ONTOLOGY_TERM_SYNONYM_ATTR), "height");

    assertTrue(i.hasNext());
    entity = i.next();
    assertNotNull(entity.get(OntologyTermSynonymMetadata.ID));
    assertEquals(entity.get(OntologyTermSynonymMetadata.ONTOLOGY_TERM_SYNONYM_ATTR), "hospital");

    assertFalse(i.hasNext());
  }

  @Test
  public void ontologyTermRepositoryIterator() throws OWLOntologyCreationException {
    Entity entityOntology = ontologyRepository.iterator().next();

    Iterator<Entity> i = ontologyTermRepository.iterator();
    assertTrue(i.hasNext());

    // Organization
    Entity entity = i.next();
    assertNotNull(entity.get(OntologyTermMetadata.ID));
    assertEquals(
        entity.get(OntologyTermMetadata.ONTOLOGY_TERM_IRI), "http://www.molgenis.org#Organization");
    assertEquals(entity.get(OntologyTermMetadata.ONTOLOGY_TERM_NAME), "organization");
    assertEquals(
        getMrefAttributeList(
            entity,
            OntologyTermMetadata.ONTOLOGY_TERM_SYNONYM,
            OntologyTermSynonymMetadata.ONTOLOGY_TERM_SYNONYM_ATTR),
        singletonList("organization"));
    assertEquals(
        getMrefAttributeList(
            entity,
            OntologyTermMetadata.ONTOLOGY_TERM_DYNAMIC_ANNOTATION,
            OntologyTermDynamicAnnotationMetadata.LABEL),
        emptyList());
    assertEquals(
        getMrefAttributeList(
            entity,
            OntologyTermMetadata.ONTOLOGY_TERM_NODE_PATH,
            OntologyTermNodePathMetadata.NODE_PATH),
        singletonList("0[0].0[1]"));
    assertEquals(entity.get(OntologyTermMetadata.ONTOLOGY), entityOntology);

    // Team
    entity = i.next();
    assertNotNull(entity.get(OntologyTermMetadata.ID));
    assertEquals(
        entity.get(OntologyTermMetadata.ONTOLOGY_TERM_IRI), "http://www.molgenis.org#Team");
    assertEquals(entity.get(OntologyTermMetadata.ONTOLOGY_TERM_NAME), "team");
    assertEquals(
        getMrefAttributeList(
            entity,
            OntologyTermMetadata.ONTOLOGY_TERM_SYNONYM,
            OntologyTermSynonymMetadata.ONTOLOGY_TERM_SYNONYM_ATTR),
        singletonList("team"));
    assertEquals(
        getMrefAttributeList(
            entity,
            OntologyTermMetadata.ONTOLOGY_TERM_DYNAMIC_ANNOTATION,
            OntologyTermDynamicAnnotationMetadata.LABEL),
        asList("friday:2412423", "molgenis:1231424"));
    assertEquals(
        getMrefAttributeList(
            entity,
            OntologyTermMetadata.ONTOLOGY_TERM_NODE_PATH,
            OntologyTermNodePathMetadata.NODE_PATH),
        singletonList("0[0].1[1]"));
    assertEquals(entity.get(OntologyTermMetadata.ONTOLOGY), entityOntology);

    // GCC
    entity = i.next();
    assertNotNull(entity.get(OntologyTermMetadata.ID));
    assertEquals(entity.get(OntologyTermMetadata.ONTOLOGY_TERM_IRI), "http://www.molgenis.org#GCC");
    assertEquals(
        entity.get(OntologyTermMetadata.ONTOLOGY_TERM_NAME), "Genomics coordination center");
    assertEquals(
        getMrefAttributeList(
            entity,
            OntologyTermMetadata.ONTOLOGY_TERM_SYNONYM,
            OntologyTermSynonymMetadata.ONTOLOGY_TERM_SYNONYM_ATTR),
        singletonList("Genomics coordination center"));
    assertEquals(
        getMrefAttributeList(
            entity,
            OntologyTermMetadata.ONTOLOGY_TERM_DYNAMIC_ANNOTATION,
            OntologyTermDynamicAnnotationMetadata.LABEL),
        asList("GCC:987654", "GCC:123456"));
    assertEquals(
        getMrefAttributeList(
            entity,
            OntologyTermMetadata.ONTOLOGY_TERM_NODE_PATH,
            OntologyTermNodePathMetadata.NODE_PATH),
        asList("0[0].0[1].0[2].0[3]", "0[0].1[1].0[2]"));
    assertEquals(entity.get(OntologyTermMetadata.ONTOLOGY), entityOntology);

    // Weight
    entity = i.next();
    assertNotNull(entity.get(OntologyTermMetadata.ID));
    assertEquals(
        entity.get(OntologyTermMetadata.ONTOLOGY_TERM_IRI), "http://www.molgenis.org#weight");
    assertEquals(entity.get(OntologyTermMetadata.ONTOLOGY_TERM_NAME), "weight");
    assertEquals(
        getMrefAttributeList(
            entity,
            OntologyTermMetadata.ONTOLOGY_TERM_SYNONYM,
            OntologyTermSynonymMetadata.ONTOLOGY_TERM_SYNONYM_ATTR),
        singletonList("weight"));
    assertEquals(
        getMrefAttributeList(
            entity,
            OntologyTermMetadata.ONTOLOGY_TERM_DYNAMIC_ANNOTATION,
            OntologyTermDynamicAnnotationMetadata.LABEL),
        emptyList());
    assertEquals(
        getMrefAttributeList(
            entity,
            OntologyTermMetadata.ONTOLOGY_TERM_NODE_PATH,
            OntologyTermNodePathMetadata.NODE_PATH),
        singletonList("0[0].2[1].0[2]"));
    assertEquals(entity.get(OntologyTermMetadata.ONTOLOGY), entityOntology);

    // Top
    entity = i.next();
    assertNotNull(entity.get(OntologyTermMetadata.ID));
    assertEquals(entity.get(OntologyTermMetadata.ONTOLOGY_TERM_IRI), "top");
    assertEquals(entity.get(OntologyTermMetadata.ONTOLOGY_TERM_NAME), "top");
    assertEquals(
        getMrefAttributeList(
            entity,
            OntologyTermMetadata.ONTOLOGY_TERM_SYNONYM,
            OntologyTermSynonymMetadata.ONTOLOGY_TERM_SYNONYM_ATTR),
        singletonList("top"));
    assertEquals(
        getMrefAttributeList(
            entity,
            OntologyTermMetadata.ONTOLOGY_TERM_DYNAMIC_ANNOTATION,
            OntologyTermDynamicAnnotationMetadata.LABEL),
        emptyList());
    assertEquals(
        getMrefAttributeList(
            entity,
            OntologyTermMetadata.ONTOLOGY_TERM_NODE_PATH,
            OntologyTermNodePathMetadata.NODE_PATH),
        singletonList("0[0]"));
    assertEquals(entity.get(OntologyTermMetadata.ONTOLOGY), entityOntology);

    // Measurement
    entity = i.next();
    assertNotNull(entity.get(OntologyTermMetadata.ID));
    assertEquals(
        entity.get(OntologyTermMetadata.ONTOLOGY_TERM_IRI), "http://www.molgenis.org#measurement");
    assertEquals(entity.get(OntologyTermMetadata.ONTOLOGY_TERM_NAME), "measurement");
    assertEquals(
        getMrefAttributeList(
            entity,
            OntologyTermMetadata.ONTOLOGY_TERM_SYNONYM,
            OntologyTermSynonymMetadata.ONTOLOGY_TERM_SYNONYM_ATTR),
        singletonList("measurement"));
    assertEquals(
        getMrefAttributeList(
            entity,
            OntologyTermMetadata.ONTOLOGY_TERM_DYNAMIC_ANNOTATION,
            OntologyTermDynamicAnnotationMetadata.LABEL),
        emptyList());
    assertEquals(
        getMrefAttributeList(
            entity,
            OntologyTermMetadata.ONTOLOGY_TERM_NODE_PATH,
            OntologyTermNodePathMetadata.NODE_PATH),
        singletonList("0[0].2[1]"));
    assertEquals(entity.get(OntologyTermMetadata.ONTOLOGY), entityOntology);

    // Body length
    entity = i.next();
    assertNotNull(entity.get(OntologyTermMetadata.ID));
    assertEquals(
        entity.get(OntologyTermMetadata.ONTOLOGY_TERM_IRI), "http://www.molgenis.org#body_length");
    assertEquals(entity.get(OntologyTermMetadata.ONTOLOGY_TERM_NAME), "body length");
    assertEquals(
        getMrefAttributeList(
            entity,
            OntologyTermMetadata.ONTOLOGY_TERM_SYNONYM,
            OntologyTermSynonymMetadata.ONTOLOGY_TERM_SYNONYM_ATTR),
        singletonList("body length"));
    assertEquals(
        getMrefAttributeList(
            entity,
            OntologyTermMetadata.ONTOLOGY_TERM_DYNAMIC_ANNOTATION,
            OntologyTermDynamicAnnotationMetadata.LABEL),
        emptyList());
    assertEquals(
        getMrefAttributeList(
            entity,
            OntologyTermMetadata.ONTOLOGY_TERM_NODE_PATH,
            OntologyTermNodePathMetadata.NODE_PATH),
        singletonList("0[0].2[1].1[2].0[3]"));
    assertEquals(entity.get(OntologyTermMetadata.ONTOLOGY), entityOntology);

    // Height
    entity = i.next();
    assertNotNull(entity.get(OntologyTermMetadata.ID));
    assertEquals(
        entity.get(OntologyTermMetadata.ONTOLOGY_TERM_IRI), "http://www.molgenis.org#height");
    assertEquals(entity.get(OntologyTermMetadata.ONTOLOGY_TERM_NAME), "height");
    assertEquals(
        getMrefAttributeList(
            entity,
            OntologyTermMetadata.ONTOLOGY_TERM_SYNONYM,
            OntologyTermSynonymMetadata.ONTOLOGY_TERM_SYNONYM_ATTR),
        singletonList("height"));
    assertEquals(
        getMrefAttributeList(
            entity,
            OntologyTermMetadata.ONTOLOGY_TERM_DYNAMIC_ANNOTATION,
            OntologyTermDynamicAnnotationMetadata.LABEL),
        emptyList());
    assertEquals(
        getMrefAttributeList(
            entity,
            OntologyTermMetadata.ONTOLOGY_TERM_NODE_PATH,
            OntologyTermNodePathMetadata.NODE_PATH),
        singletonList("0[0].2[1].1[2]"));
    assertEquals(entity.get(OntologyTermMetadata.ONTOLOGY), entityOntology);

    // Hospital
    entity = i.next();
    assertNotNull(entity.get(OntologyTermMetadata.ID));
    assertEquals(
        entity.get(OntologyTermMetadata.ONTOLOGY_TERM_IRI), "http://www.molgenis.org#hospital");
    assertEquals(entity.get(OntologyTermMetadata.ONTOLOGY_TERM_NAME), "hospital");
    assertEquals(
        getMrefAttributeList(
            entity,
            OntologyTermMetadata.ONTOLOGY_TERM_SYNONYM,
            OntologyTermSynonymMetadata.ONTOLOGY_TERM_SYNONYM_ATTR),
        singletonList("hospital"));
    assertEquals(
        getMrefAttributeList(
            entity,
            OntologyTermMetadata.ONTOLOGY_TERM_DYNAMIC_ANNOTATION,
            OntologyTermDynamicAnnotationMetadata.LABEL),
        emptyList());
    assertEquals(
        getMrefAttributeList(
            entity,
            OntologyTermMetadata.ONTOLOGY_TERM_NODE_PATH,
            OntologyTermNodePathMetadata.NODE_PATH),
        singletonList("0[0].0[1].0[2]"));
    assertEquals(entity.get(OntologyTermMetadata.ONTOLOGY), entityOntology);

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
  public static class Config {
    @Bean
    public IdGenerator idGenerator() {
      IdGenerator idGenerator = mock(IdGenerator.class);
      when(idGenerator.generateId()).thenAnswer((invocation) -> UUID.randomUUID().toString());
      return idGenerator;
    }
  }
}
