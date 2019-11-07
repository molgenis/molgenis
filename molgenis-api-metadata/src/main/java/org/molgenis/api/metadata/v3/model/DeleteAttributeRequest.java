package org.molgenis.api.metadata.v3.model;

import javax.validation.constraints.NotNull;

public class DeleteAttributeRequest {
  @NotNull private String entityTypeId;
  @NotNull private String attributeId;

  public String getEntityTypeId() {
    return entityTypeId;
  }

  public void setEntityTypeId(String entityTypeId) {
    this.entityTypeId = entityTypeId;
  }

  public String getAttributeId() {
    return attributeId;
  }

  public void setAttributeId(String attributeId) {
    this.attributeId = attributeId;
  }
}
