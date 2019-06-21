package org.molgenis.api.data.v3;

import javax.validation.constraints.NotNull;

public class EntityRequest extends BaseEntityRequest {
  @NotNull private String entityId;

  public String getEntityId() {
    return entityId;
  }

  public void setEntityId(String entityId) {
    this.entityId = entityId;
  }
}
