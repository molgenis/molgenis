package org.molgenis.dataexplorer.negotiator.config;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

public class NegotiatorEntityConfig extends StaticEntity {
  public NegotiatorEntityConfig(Entity entity) {
    super(entity);
  }

  public NegotiatorEntityConfig(EntityType entityType) {
    super(entityType);
  }

  public NegotiatorEntityConfig(String identifier, EntityType entityType) {
    super(identifier, entityType);
  }

  public NegotiatorConfig getNegotiatorConfig() {
    return getEntity(NegotiatorEntityConfigMetadata.NEGOTIATOR_CONFIG, NegotiatorConfig.class);
  }
}
