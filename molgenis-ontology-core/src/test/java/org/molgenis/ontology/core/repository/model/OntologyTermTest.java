package org.molgenis.ontology.core.repository.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.molgenis.ontology.core.model.OntologyTerm;

class OntologyTermTest {
  @Test
  void testAnd() {
    OntologyTerm term1 = OntologyTerm.create("iri1", "label 1");
    OntologyTerm term2 = OntologyTerm.create("iri2", "label 2");
    OntologyTerm term3 = OntologyTerm.create("iri3", "label 3");
    assertEquals(
        OntologyTerm.create("iri1,iri2", "(label 1 and label 2)"), OntologyTerm.and(term1, term2));
    assertEquals(
        OntologyTerm.create("iri1,iri2,iri3", "(label 1 and label 2 and label 3)"),
        OntologyTerm.and(term1, term2, term3));
  }
}
