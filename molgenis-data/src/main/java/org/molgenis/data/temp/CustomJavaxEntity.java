package org.molgenis.data.temp;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

@ExcludeFromCodeCoverageJavax
public class CustomJavaxEntity extends StaticEntity {

  public CustomJavaxEntity(Entity entity) {
    super(entity);
  }

  public CustomJavaxEntity(EntityType entityType) {
    super(entityType);
  }

  public CustomJavaxEntity(String id, EntityType entityType) {
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
