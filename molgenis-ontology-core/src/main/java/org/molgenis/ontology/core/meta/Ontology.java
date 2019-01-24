package org.molgenis.ontology.core.meta;

import static org.molgenis.ontology.core.meta.OntologyMetadata.ID;
import static org.molgenis.ontology.core.meta.OntologyMetadata.ONTOLOGY_IRI;
import static org.molgenis.ontology.core.meta.OntologyMetadata.ONTOLOGY_NAME;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

public class Ontology extends StaticEntity {
  public Ontology(Entity entity) {
    super(entity);
  }

  public Ontology(EntityType entityType) {
    super(entityType);
  }

  public Ontology(String id, EntityType entityType) {
    super(entityType);
    setId(id);
  }

  public String getId() {
    return getString(ID);
  }

  public void setId(String id) {
    set(ID, id);
  }

  public String getOntologyIri() {
    return getString(ONTOLOGY_IRI);
  }

  public void setOntologyIri(String ontologyIri) {
    set(ONTOLOGY_IRI, ontologyIri);
  }

  public String getOntologyName() {
    return getString(ONTOLOGY_NAME);
  }

  public void setOntologyName(String ontologyName) {
    set(ONTOLOGY_NAME, ontologyName);
  }
}
