package org.molgenis.data.temp;

import lombok.Generated;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

@Generated
public class LombokEntity extends StaticEntity {

  public LombokEntity(Entity entity) {
    super(entity);
  }

  public LombokEntity(EntityType entityType) {
    super(entityType);
  }

  public LombokEntity(String id, EntityType entityType) {
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
