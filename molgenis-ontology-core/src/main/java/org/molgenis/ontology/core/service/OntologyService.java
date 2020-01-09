package org.molgenis.ontology.core.service;

import java.util.List;
import java.util.Set;
import org.molgenis.ontology.core.model.Ontology;
import org.molgenis.ontology.core.model.OntologyTerm;

public interface OntologyService {

  /** Retrieves all ontologies. */
  List<Ontology> getOntologies();

  /** Retrieves ontologies with the given id. */
  List<Ontology> getOntologies(List<String> ontologyIds);

  /**
   * Retrieves a specific ontology
   *
   * @param iri IRI of the ontology to retrieve.
   * @return the Ontology
   */
  Ontology getOntology(String iri);

  /**
   * Finds ontology terms that are exact matches to a certain search string.
   *
   * @param pageSize number of results to return.
   * @return List of {@link OntologyTerm}s that match the search term.
   */
  List<OntologyTerm> findExactOntologyTerms(
      List<String> ontologyIds, Set<String> terms, int pageSize);

  /**
   * Finds ontology terms that match a certain search string.
   *
   * @param pageSize number of results to return.
   * @return List of {@link OntologyTerm}s that match the search term.
   */
  List<OntologyTerm> findOntologyTerms(List<String> ontologyIds, Set<String> terms, int pageSize);

  /**
   * Retrieves a specific OntologyTerm
   *
   * @return Combined {@link OntologyTerm} for all IRI's listed
   */
  OntologyTerm getOntologyTerm(String iri);

  /**
   * Retrieves all children from the current ontology term
   *
   * @return a list of {@link OntologyTerm} as children
   */
  List<OntologyTerm> getChildren(OntologyTerm ontologyTerm);

  /** Calculate distance between two ontology terms */
  Integer getOntologyTermDistance(OntologyTerm ontologyTerm1, OntologyTerm ontologyTerm2);

  /**
   * Retrieves all ontologies ids.
   *
   * @return String Ontology Id
   */
  List<String> getAllOntologiesIds();
}
