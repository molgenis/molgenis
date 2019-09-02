package org.molgenis.api.meta.model;

public class ReadEntityTypeRequest extends AbstractReadRequest {

  private String entityTypeId;

  public String getEntityTypeId() {
    return entityTypeId;
  }

  public void setEntityTypeId(String entityTypeId) {
    this.entityTypeId = entityTypeId;
  }
}
