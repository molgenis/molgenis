package org.molgenis.api.data.v3.model;

import javax.validation.constraints.NotNull;

public class DeleteEntityRequest extends AbstractEntityTypeRequest {
  @NotNull private String entityId;

  public String getEntityId() {
    return entityId;
  }

  public void setEntityId(String entityId) {
    this.entityId = entityId;
  }
}
