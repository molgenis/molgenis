package org.molgenis.dataexplorer.negotiator.config;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
class NegotiatorEntityConfigFactory
    extends AbstractSystemEntityFactory<
        NegotiatorEntityConfig, NegotiatorEntityConfigMetadata, String> {
  NegotiatorEntityConfigFactory(
      NegotiatorEntityConfigMetadata myEntityMeta, EntityPopulator entityPopulator) {
    super(NegotiatorEntityConfig.class, myEntityMeta, entityPopulator);
  }
}
