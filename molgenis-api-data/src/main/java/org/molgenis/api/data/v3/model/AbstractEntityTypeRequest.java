package org.molgenis.api.data.v3.model;

import javax.validation.constraints.NotNull;

abstract class AbstractEntityTypeRequest {
  @NotNull private String entityTypeId;

  public String getEntityTypeId() {
    return entityTypeId;
  }

  public void setEntityTypeId(String entityTypeId) {
    this.entityTypeId = entityTypeId;
  }
}
