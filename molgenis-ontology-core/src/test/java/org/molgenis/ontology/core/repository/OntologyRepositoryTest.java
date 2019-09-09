package org.molgenis.ontology.core.repository;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.ontology.core.meta.OntologyMetadata.ID;
import static org.molgenis.ontology.core.meta.OntologyMetadata.ONTOLOGY;
import static org.molgenis.ontology.core.meta.OntologyMetadata.ONTOLOGY_IRI;
import static org.molgenis.ontology.core.meta.OntologyMetadata.ONTOLOGY_NAME;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.ontology.core.model.Ontology;
import org.molgenis.test.AbstractMockitoSpringContextTests;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = OntologyRepositoryTest.Config.class)
class OntologyRepositoryTest extends AbstractMockitoSpringContextTests {
  @Autowired private DataService dataService;

  @Autowired private OntologyRepository ontologyRepository;

  private org.molgenis.ontology.core.meta.Ontology ontologyEntity;

  @Test
  void testGetOntologies() {
    ontologyEntity = mock(org.molgenis.ontology.core.meta.Ontology.class);
    when(ontologyEntity.getString(ID)).thenReturn("1");
    when(ontologyEntity.getString(ONTOLOGY_IRI)).thenReturn("http://www.ontology.com/test");
    when(ontologyEntity.getString(ONTOLOGY_NAME)).thenReturn("testOntology");

    when(dataService.findAll(eq(ONTOLOGY))).thenReturn(Stream.of(ontologyEntity));
    List<Ontology> ontologies = ontologyRepository.getOntologies().collect(toList());
    assertEquals(
        ontologies, asList(Ontology.create("1", "http://www.ontology.com/test", "testOntology")));
  }

  @Test
  void testGetOntologiesList() {
    ontologyEntity = mock(org.molgenis.ontology.core.meta.Ontology.class);
    when(ontologyEntity.getString(ID)).thenReturn("1");
    when(ontologyEntity.getString(ONTOLOGY_IRI)).thenReturn("http://www.ontology.com/test");
    when(ontologyEntity.getString(ONTOLOGY_NAME)).thenReturn("testOntology");

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Object>> idCaptor = ArgumentCaptor.forClass(Stream.class);
    when(dataService.findAll(
            eq(ONTOLOGY), idCaptor.capture(), eq(org.molgenis.ontology.core.meta.Ontology.class)))
        .thenReturn(Stream.of(ontologyEntity));
    List<Ontology> ontologies =
        ontologyRepository.getOntologies(singletonList("1")).collect(toList());
    assertEquals(
        ontologies, asList(Ontology.create("1", "http://www.ontology.com/test", "testOntology")));
    assertEquals(idCaptor.getValue().collect(toList()), singletonList("1"));
  }

  @Test
  void testGetOntology() {
    ontologyEntity = mock(org.molgenis.ontology.core.meta.Ontology.class);
    when(ontologyEntity.getString(ID)).thenReturn("1");
    when(ontologyEntity.getString(ONTOLOGY_IRI)).thenReturn("http://www.ontology.com/test");
    when(ontologyEntity.getString(ONTOLOGY_NAME)).thenReturn("testOntology");

    @SuppressWarnings("unchecked")
    Query<org.molgenis.ontology.core.meta.Ontology> query = mock(Query.class, RETURNS_SELF);
    when(dataService.query(ONTOLOGY, org.molgenis.ontology.core.meta.Ontology.class))
        .thenReturn(query);
    when(query.eq(ONTOLOGY_IRI, "http://www.ontology.com/test").findOne())
        .thenReturn(ontologyEntity);
    assertEquals(
        ontologyRepository.getOntology("http://www.ontology.com/test"),
        Ontology.create("1", "http://www.ontology.com/test", "testOntology"));
  }

  @Configuration
  static class Config {
    @Bean
    DataService dataService() {
      return mock(DataService.class);
    }

    @Bean
    OntologyRepository ontologyRepository() {
      return new OntologyRepository();
    }
  }
}
