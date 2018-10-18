package org.molgenis.data.temp;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

@ExcludeFromCodeCoverageLombok
public class CustomLombokEntity extends StaticEntity {

  public CustomLombokEntity(Entity entity) {
    super(entity);
  }

  public CustomLombokEntity(EntityType entityType) {
    super(entityType);
  }

  public CustomLombokEntity(String id, EntityType entityType) {
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
