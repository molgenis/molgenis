package org.molgenis.api.metadata.v3.model;

public class ReadEntityTypeRequest {
  private String entityTypeId;
  private boolean flattenAttrs;

  public String getEntityTypeId() {
    return entityTypeId;
  }

  public void setEntityTypeId(String entityTypeId) {
    this.entityTypeId = entityTypeId;
  }

  public boolean isFlattenAttrs() {
    return flattenAttrs;
  }

  public void setFlattenAttrs(boolean flattenAttrs) {
    this.flattenAttrs = flattenAttrs;
  }
}
