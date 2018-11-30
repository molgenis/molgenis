package org.molgenis.ontology.core.repository;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.ontology.core.meta.OntologyMetaData.ID;
import static org.molgenis.ontology.core.meta.OntologyMetaData.ONTOLOGY;
import static org.molgenis.ontology.core.meta.OntologyMetaData.ONTOLOGY_IRI;
import static org.molgenis.ontology.core.meta.OntologyMetaData.ONTOLOGY_NAME;
import static org.molgenis.ontology.core.meta.OntologyMetaData.SIMPLE_NAME;
import static org.testng.Assert.assertEquals;

import java.util.List;
import java.util.stream.Stream;
import org.mockito.ArgumentCaptor;
import org.molgenis.data.DataService;
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
    when(dataService
            .query(ONTOLOGY, org.molgenis.ontology.core.meta.Ontology.class)
            .eq(ONTOLOGY_IRI, "http://www.ontology.com/test")
            .findOne())
        .thenReturn(ontologyEntity);
    assertEquals(
        ontologyRepository.getOntology("http://www.ontology.com/test"),
        Ontology.create("1", "http://www.ontology.com/test", "testOntology"));
  }

  @Configuration
  public static class Config {
    @Bean
    public DataService dataService() {
      return mock(DataService.class, RETURNS_DEEP_STUBS);
    }

    @Bean
    public OntologyRepository ontologyRepository() {
      return new OntologyRepository();
    }
  }
}
