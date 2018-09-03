package org.molgenis.ontology.core.repository;

import static org.molgenis.ontology.core.meta.OntologyMetaData.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.ontology.core.model.Ontology;
import org.springframework.beans.factory.annotation.Autowired;

/** Maps OntologyMetaData {@link Entity} <-> {@link Ontology} */
public class OntologyRepository {
  @Autowired private DataService dataService;

  /** Retrieves all {@link Ontology}s. */
  public Stream<Ontology> getOntologies() {
    return dataService.findAll(ONTOLOGY).map(OntologyRepository::toOntology);
  }

  /** Retrieves all {@link Ontology}s with the given ids. */
  public Stream<Ontology> getOntologies(List<String> ontologyIds) {
    return dataService
        .findAll(
            ONTOLOGY,
            new ArrayList<Object>(ontologyIds).stream(),
            org.molgenis.ontology.core.meta.Ontology.class)
        .map(OntologyRepository::toOntology);
  }

  /**
   * Retrieves an ontology with a specific IRI.
   *
   * @param iri the IRI of the ontology
   */
  public Ontology getOntology(String iri) {
    return toOntology(dataService.findOne(ONTOLOGY, QueryImpl.EQ(ONTOLOGY_IRI, iri)));
  }

  private static Ontology toOntology(Entity entity) {
    if (entity == null) {
      return null;
    }
    return Ontology.create(
        entity.getString(ID), entity.getString(ONTOLOGY_IRI), entity.getString(ONTOLOGY_NAME));
  }
}
