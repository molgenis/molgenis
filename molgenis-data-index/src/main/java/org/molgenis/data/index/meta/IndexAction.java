package org.molgenis.data.index.meta;

import static org.molgenis.data.index.meta.IndexActionMetadata.ACTION_ORDER;
import static org.molgenis.data.index.meta.IndexActionMetadata.ENTITY_ID;
import static org.molgenis.data.index.meta.IndexActionMetadata.ENTITY_TYPE_ID;
import static org.molgenis.data.index.meta.IndexActionMetadata.ID;
import static org.molgenis.data.index.meta.IndexActionMetadata.INDEX_ACTION_GROUP_ATTR;
import static org.molgenis.data.index.meta.IndexActionMetadata.INDEX_STATUS;
import static org.molgenis.data.index.meta.IndexActionMetadata.IndexStatus;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

public class IndexAction extends StaticEntity {
  public IndexAction(Entity entity) {
    super(entity);
  }

  public IndexAction(EntityType entityType) {
    super(entityType);
  }

  public IndexAction(String id, EntityType entityType) {
    super(entityType);
    set(ID, id);
  }

  public String getId() {
    return getString(ID);
  }

  public IndexAction setId(String id) {
    set(ID, id);
    return this;
  }

  @Nullable
  @CheckForNull
  public IndexActionGroup getIndexActionGroup() {
    return getEntity(INDEX_ACTION_GROUP_ATTR, IndexActionGroup.class);
  }

  public IndexAction setIndexActionGroup(IndexActionGroup indexActionGroup) {
    set(INDEX_ACTION_GROUP_ATTR, indexActionGroup);
    return this;
  }

  public int getActionOrder() {
    return getInt(ACTION_ORDER);
  }

  public IndexAction setActionOrder(int actionOrder) {
    set(ACTION_ORDER, actionOrder);
    return this;
  }

  public String getEntityTypeId() {
    return getString(ENTITY_TYPE_ID);
  }

  public IndexAction setEntityTypeId(String entityTypeId) {
    set(ENTITY_TYPE_ID, entityTypeId);
    return this;
  }

  @Nullable
  @CheckForNull
  public String getEntityId() {
    return getString(ENTITY_ID);
  }

  public IndexAction setEntityId(String entityId) {
    set(ENTITY_ID, entityId);
    return this;
  }

  public IndexActionMetadata.IndexStatus getIndexStatus() {
    String indexStatusStr = getString(INDEX_STATUS);
    return indexStatusStr != null ? IndexStatus.valueOf(indexStatusStr) : null;
  }

  public IndexAction setIndexStatus(IndexActionMetadata.IndexStatus indexStatus) {
    set(INDEX_STATUS, indexStatus.toString());
    return this;
  }

  /**
   * Returns whether two index actions are equal ignoring the auto id
   *
   * @param o other
   * @return {@code true} if this object is the same as the o argument; {@code false} otherwise.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    IndexAction that = (IndexAction) o;

    String entityId = getEntityId();
    if (entityId != null ? !entityId.equals(that.getEntityId()) : that.getEntityId() != null)
      return false;
    return getEntityTypeId() != null
        ? getEntityTypeId().equals(that.getEntityTypeId())
        : that.getEntityTypeId() == null;
  }

  /**
   * Returns a hash code value for this index action ignoring the auto id
   *
   * @return a hash code value for this object.
   */
  @Override
  public int hashCode() {
    String entityId = getEntityId();
    int result = entityId != null ? entityId.hashCode() : 0;
    result = 31 * result + (getEntityTypeId() != null ? getEntityTypeId().hashCode() : 0);
    return result;
  }

  public boolean isWholeRepository() {
    return getEntityId() == null;
  }
}
