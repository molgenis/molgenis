package org.molgenis.ontology.core.meta;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class OntologyTermSynonymFactory
    extends AbstractSystemEntityFactory<OntologyTermSynonym, OntologyTermSynonymMetadata, String> {
  OntologyTermSynonymFactory(
      OntologyTermSynonymMetadata ontologyTermSynonymMetadata, EntityPopulator entityPopulator) {
    super(OntologyTermSynonym.class, ontologyTermSynonymMetadata, entityPopulator);
  }
}
