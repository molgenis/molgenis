package org.molgenis.api.data.v3.model;

import javax.validation.constraints.NotNull;

public class ReadSubresourceRequest extends ReadEntitiesRequest {
  @NotNull private String entityId;

  @NotNull private String fieldId;

  public String getEntityId() {
    return entityId;
  }

  public void setEntityId(String entityId) {
    this.entityId = entityId;
  }

  public String getFieldId() {
    return fieldId;
  }

  public void setFieldId(String fieldId) {
    this.fieldId = fieldId;
  }
}
