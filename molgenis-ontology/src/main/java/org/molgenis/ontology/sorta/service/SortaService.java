package org.molgenis.ontology.sorta.service;

import org.molgenis.data.Entity;

public interface SortaService {
  /**
   * Get all ontology data in generic type
   *
   * @return list of untyped ontology data
   */
  Iterable<Entity> getAllOntologyEntities();

  /**
   * Get a specified ontology entity in generic type based on the given ontologyIri
   *
   * @return specified ontology entity in generic type
   */
  Entity getOntologyEntity(String ontologyIri);

  /**
   * Get a specified ontologyterm in generic type based on the given ontologyIri and ontologyTermIri
   *
   * @return specifieid ontologyterm entity in a generic type
   */
  Entity getOntologyTermEntity(String ontologyTermIri, String ontologyIri);

  /**
   * Find a list of relevant ontologyterm typed data using lexical matching (elasticsearch + ngram)
   * in generic type based on given ontologyIri and a set of query inputs (name, synonym, ontology
   * database id, e.g. hpo, omim)
   *
   * @return a list of ontologyterm data in generic type
   */
  Iterable<Entity> findOntologyTermEntities(String ontologyIri, Entity inputEntity);
}
