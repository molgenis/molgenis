package org.molgenis.data.temp;

import javax.annotation.Generated;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

@Generated(value = "test")
public class JavaxEntity extends StaticEntity {

  public JavaxEntity(Entity entity) {
    super(entity);
  }

  public JavaxEntity(EntityType entityType) {
    super(entityType);
  }

  public JavaxEntity(String id, EntityType entityType) {
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
