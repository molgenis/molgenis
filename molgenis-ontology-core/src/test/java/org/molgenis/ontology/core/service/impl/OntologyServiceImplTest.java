package org.molgenis.ontology.core.service.impl;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.ontology.core.model.Ontology;
import org.molgenis.ontology.core.repository.OntologyRepository;
import org.molgenis.ontology.core.repository.OntologyTermRepository;
import org.molgenis.test.AbstractMockitoTest;

class OntologyServiceImplTest extends AbstractMockitoTest {

  @Mock private OntologyRepository ontologyRepository;
  @Mock private OntologyTermRepository ontologyTermRepository;

  private OntologyServiceImpl ontologyServiceImpl;

  @BeforeEach
  void setUpBeforeMethod() {
    ontologyServiceImpl = new OntologyServiceImpl(ontologyRepository, ontologyTermRepository);
  }

  @Test
  void testGetOntologiesList() {
    List<String> ontologyIds = asList("id0", "id1");
    Ontology ontology0 = mock(Ontology.class);
    Ontology ontology1 = mock(Ontology.class);
    when(ontologyRepository.getOntologies(ontologyIds)).thenReturn(Stream.of(ontology0, ontology1));
    assertEquals(ontologyServiceImpl.getOntologies(ontologyIds), asList(ontology0, ontology1));
  }
}
