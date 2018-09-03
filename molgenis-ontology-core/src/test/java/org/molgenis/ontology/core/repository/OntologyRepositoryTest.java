package org.molgenis.ontology.core.repository;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.ontology.core.meta.OntologyMetaData.*;
import static org.testng.Assert.assertEquals;

import java.util.List;
import java.util.stream.Stream;
import org.mockito.ArgumentCaptor;
import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.core.model.Ontology;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

@ContextConfiguration(classes = OntologyRepositoryTest.Config.class)
public class OntologyRepositoryTest extends AbstractTestNGSpringContextTests {
  @Autowired private DataService dataService;

  @Autowired private OntologyRepository ontologyRepository;

  private org.molgenis.ontology.core.meta.Ontology ontologyEntity;

  @BeforeTest
  public void beforeTest() {
    ontologyEntity = mock(org.molgenis.ontology.core.meta.Ontology.class);
    when(ontologyEntity.getString(ID)).thenReturn("1");
    when(ontologyEntity.getString(ONTOLOGY_IRI)).thenReturn("http://www.ontology.com/test");
    when(ontologyEntity.getString(ONTOLOGY_NAME)).thenReturn("testOntology");
    when(ontologyEntity.getString(SIMPLE_NAME)).thenReturn("test");
  }

  @Test
  public void testGetOntologies() {
    when(dataService.findAll(eq(ONTOLOGY))).thenReturn(Stream.of(ontologyEntity));
    List<Ontology> ontologies = ontologyRepository.getOntologies().collect(toList());
    assertEquals(
        ontologies, asList(Ontology.create("1", "http://www.ontology.com/test", "testOntology")));
  }

  @Test
  public void testGetOntologiesList() {
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
  public void testGetOntology() {
    when(dataService.findOne(ONTOLOGY, QueryImpl.EQ(ONTOLOGY_IRI, "http://www.ontology.com/test")))
        .thenReturn(ontologyEntity);
    assertEquals(
        ontologyRepository.getOntology("http://www.ontology.com/test"),
        Ontology.create("1", "http://www.ontology.com/test", "testOntology"));
  }

  @Configuration
  public static class Config {
    @Bean
    public DataService dataService() {
      return mock(DataService.class);
    }

    @Bean
    public OntologyRepository ontologyRepository() {
      return new OntologyRepository();
    }
  }
}
