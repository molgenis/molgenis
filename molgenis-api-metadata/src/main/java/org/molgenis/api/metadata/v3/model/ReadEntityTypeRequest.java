package org.molgenis.api.metadata.v3.model;

public class ReadEntityTypeRequest {
  private String entityTypeId;
  private boolean flattenAttrs;
  private boolean i18n;

  public String getEntityTypeId() {
    return entityTypeId;
  }

  public void setEntityTypeId(String entityTypeId) {
    this.entityTypeId = entityTypeId;
  }

  public boolean isI18n() {
    return i18n;
  }

  public void setI18n(boolean i18n) {
    this.i18n = i18n;
  }

  public boolean isFlattenAttrs() {
    return flattenAttrs;
  }

  public void setFlattenAttrs(boolean flattenAttrs) {
    this.flattenAttrs = flattenAttrs;
  }
}
