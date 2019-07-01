package org.molgenis.api.data.v3;

import javax.validation.constraints.NotNull;

public class DeleteEntityRequest {
  @NotNull private String entityTypeId;
  @NotNull private String entityId;

  public String getEntityTypeId() {
    return entityTypeId;
  }

  public void setEntityTypeId(String entityTypeId) {
    this.entityTypeId = entityTypeId;
  }

  public String getEntityId() {
    return entityId;
  }

  public void setEntityId(String entityId) {
    this.entityId = entityId;
  }
}
