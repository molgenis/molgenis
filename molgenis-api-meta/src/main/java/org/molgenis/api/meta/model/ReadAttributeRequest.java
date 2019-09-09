package org.molgenis.api.meta.model;

public class ReadAttributeRequest {

  private String entityTypeId;
  private String attributeId;

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
