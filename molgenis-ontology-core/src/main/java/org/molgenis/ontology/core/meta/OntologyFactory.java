package org.molgenis.ontology.core.meta;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class OntologyFactory
    extends AbstractSystemEntityFactory<Ontology, OntologyMetadata, String> {
  OntologyFactory(OntologyMetadata ontologyMetadata, EntityPopulator entityPopulator) {
    super(Ontology.class, ontologyMetadata, entityPopulator);
  }
}
