package org.molgenis.dataexplorer.negotiator.config;

import static org.molgenis.dataexplorer.negotiator.config.NegotiatorConfigMeta.NEGOTIATOR_URL;
import static org.molgenis.dataexplorer.negotiator.config.NegotiatorConfigMeta.PASSWORD;
import static org.molgenis.dataexplorer.negotiator.config.NegotiatorConfigMeta.USERNAME;

import javax.annotation.CheckForNull;
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

  @CheckForNull
  public String getUsername() {
    return getString(USERNAME);
  }

  @CheckForNull
  public String getPassword() {
    return getString(PASSWORD);
  }

  @CheckForNull
  public String getNegotiatorURL() {
    return getString(NEGOTIATOR_URL);
  }
}
