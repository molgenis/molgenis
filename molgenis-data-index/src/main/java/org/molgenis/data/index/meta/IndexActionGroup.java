package org.molgenis.data.index.meta;

import static org.molgenis.data.index.meta.IndexActionGroupMetadata.COUNT;
import static org.molgenis.data.index.meta.IndexActionMetadata.ID;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

public class IndexActionGroup extends StaticEntity {
  public IndexActionGroup(Entity entity) {
    super(entity);
  }

  public IndexActionGroup(EntityType entityType) {
    super(entityType);
  }

  public IndexActionGroup(String id, EntityType entityType) {
    super(entityType);
    set(ID, id);
  }

  public String getId() {
    return getString(ID);
  }

  public int getCount() {
    Integer count = getInt(COUNT);
    return count != null ? count : 0;
  }

  public IndexActionGroup setCount(int count) {
    set(COUNT, count);
    return this;
  }
}
