package org.molgenis.ontology.core.service.impl;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.ontology.core.model.Ontology;
import org.molgenis.ontology.core.model.OntologyTerm;
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
    assertEquals(asList(ontology0, ontology1), ontologyServiceImpl.getOntologies(ontologyIds));
  }

  @Test
  void testFindExactOntologyTerms() {
    List<String> ontologies = singletonList("ontology1");
    Set<String> ontologyTermStrings = singleton("term1");
    int pageSize = 100;
    List<OntologyTerm> ontologyTerms = singletonList(mock(OntologyTerm.class));
    when(ontologyTermRepository.findExcatOntologyTerms(ontologies, ontologyTermStrings, pageSize))
        .thenReturn(ontologyTerms);
    assertEquals(
        ontologyTerms,
        ontologyServiceImpl.findExactOntologyTerms(ontologies, ontologyTermStrings, pageSize));
  }

  @Test
  void testFindOntologyTerms() {
    List<String> ontologies = singletonList("ontology1");
    Set<String> ontologyTermStrings = singleton("term1");
    int pageSize = 100;
    List<OntologyTerm> ontologyTerms = singletonList(mock(OntologyTerm.class));
    when(ontologyTermRepository.findOntologyTerms(ontologies, ontologyTermStrings, pageSize))
        .thenReturn(ontologyTerms);
    assertEquals(
        ontologyTerms,
        ontologyServiceImpl.findOntologyTerms(ontologies, ontologyTermStrings, pageSize));
  }

  @Test
  void testFindOntologyTermsNoOntologies() {
    assertEquals(
        emptyList(), ontologyServiceImpl.findOntologyTerms(emptyList(), singleton("term1"), 100));
    verifyNoInteractions(ontologyRepository);
  }

  @Test
  void testFindOntologyTermsNoTerms() {
    assertEquals(
        emptyList(),
        ontologyServiceImpl.findOntologyTerms(singletonList("ontology1"), emptySet(), 100));
    verifyNoInteractions(ontologyRepository);
  }
}
