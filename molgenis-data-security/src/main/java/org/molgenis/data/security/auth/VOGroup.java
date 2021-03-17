package org.molgenis.data.security.auth;

import static org.molgenis.data.security.auth.VOGroupMetadata.ID;
import static org.molgenis.data.security.auth.VOGroupMetadata.NAME;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

@SuppressWarnings("unused")
public class VOGroup extends StaticEntity {
  public VOGroup(Entity entity) {
    super(entity);
  }

  public VOGroup(EntityType entityType) {
    super(entityType);
  }

  public VOGroup(String id, EntityType entityType) {
    super(entityType);
    setId(id);
  }

  public void setId(String id) {
    set(ID, id);
  }

  public String getId() {
    return getString(ID);
  }

  public void setName(String name) {
    set(NAME, name);
  }

  public String getName() {
    return getString(NAME);
  }
}
