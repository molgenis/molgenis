package org.molgenis.data.temp;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

public class NormalEntity extends StaticEntity {

  public NormalEntity(Entity entity) {
    super(entity);
  }

  public NormalEntity(EntityType entityType) {
    super(entityType);
  }

  public NormalEntity(String id, EntityType entityType) {
    super(entityType);
    setId(id);
  }

  public void setId(String id) {
    set("id", id);
  }

  public String getId() {
    return getString("id");
  }
}
