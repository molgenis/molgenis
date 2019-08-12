package org.molgenis.api.data.v3.model;

public class ReadSubresourceRequest extends ReadEntitiesRequest {

  private String entityId;

  private String fieldId;

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
