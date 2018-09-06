package org.molgenis.ontology.core.service.impl;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.List;
import java.util.stream.Stream;
import org.mockito.Mock;
import org.molgenis.ontology.core.model.Ontology;
import org.molgenis.ontology.core.repository.OntologyRepository;
import org.molgenis.ontology.core.repository.OntologyTermRepository;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class OntologyServiceImplTest extends AbstractMockitoTest {

  @Mock private OntologyRepository ontologyRepository;
  @Mock private OntologyTermRepository ontologyTermRepository;

  private OntologyServiceImpl ontologyServiceImpl;

  @BeforeMethod
  public void setUpBeforeMethod() {
    ontologyServiceImpl = new OntologyServiceImpl(ontologyRepository, ontologyTermRepository);
  }

  @Test
  public void testGetOntologiesList() {
    List<String> ontologyIds = asList("id0", "id1");
    Ontology ontology0 = mock(Ontology.class);
    Ontology ontology1 = mock(Ontology.class);
    when(ontologyRepository.getOntologies(ontologyIds)).thenReturn(Stream.of(ontology0, ontology1));
    assertEquals(ontologyServiceImpl.getOntologies(ontologyIds), asList(ontology0, ontology1));
  }
}
