package org.molgenis.api.metadata.v3.model;

import javax.validation.constraints.NotNull;

public class DeleteEntityTypeRequest {
  @NotNull private String entityTypeId;

  public String getEntityTypeId() {
    return entityTypeId;
  }

  public void setEntityTypeId(String entityTypeId) {
    this.entityTypeId = entityTypeId;
  }
}
