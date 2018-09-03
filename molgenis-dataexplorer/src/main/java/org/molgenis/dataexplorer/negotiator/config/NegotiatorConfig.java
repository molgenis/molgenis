package org.molgenis.dataexplorer.negotiator.config;

import static org.molgenis.dataexplorer.negotiator.config.NegotiatorConfigMeta.*;

import javax.annotation.Nullable;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

public class NegotiatorConfig extends StaticEntity {
  public NegotiatorConfig(Entity entity) {
    super(entity);
  }

  public NegotiatorConfig(EntityType entityType) {
    super(entityType);
  }

  public NegotiatorConfig(String identifier, EntityType entityType) {
    super(identifier, entityType);
  }

  @Nullable
  public String getUsername() {
    return getString(USERNAME);
  }

  @Nullable
  public String getPassword() {
    return getString(PASSWORD);
  }

  @Nullable
  public String getNegotiatorURL() {
    return getString(NEGOTIATOR_URL);
  }
}
