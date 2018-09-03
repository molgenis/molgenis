package org.molgenis.dataexplorer.negotiator.config;

import javax.annotation.Nullable;
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

  @Nullable
  public NegotiatorConfig getNegotiatorConfig() {
    return getEntity(NegotiatorEntityConfigMeta.NEGOTIATOR_CONFIG, NegotiatorConfig.class);
  }
}
