package org.molgenis.ontology.core.repository;

import static org.molgenis.ontology.core.meta.OntologyMetadata.ID;
import static org.molgenis.ontology.core.meta.OntologyMetadata.ONTOLOGY;
import static org.molgenis.ontology.core.meta.OntologyMetadata.ONTOLOGY_IRI;
import static org.molgenis.ontology.core.meta.OntologyMetadata.ONTOLOGY_NAME;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
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
    org.molgenis.ontology.core.meta.Ontology ontology =
        dataService
            .query(ONTOLOGY, org.molgenis.ontology.core.meta.Ontology.class)
            .eq(ONTOLOGY_IRI, iri)
            .findOne();
    return toOntology(ontology);
  }

  private static Ontology toOntology(Entity entity) {
    if (entity == null) {
      return null;
    }
    return Ontology.create(
        entity.getString(ID), entity.getString(ONTOLOGY_IRI), entity.getString(ONTOLOGY_NAME));
  }
}
