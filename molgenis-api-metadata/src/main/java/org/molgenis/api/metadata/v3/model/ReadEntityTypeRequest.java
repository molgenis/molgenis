package org.molgenis.api.metadata.v3.model;

public class ReadEntityTypeRequest {
  private boolean flattenAttributes;
  private boolean i18n;

  public boolean isI18n() {
    return i18n;
  }

  public void setI18n(boolean i18n) {
    this.i18n = i18n;
  }

  public boolean isFlattenAttributes() {
    return flattenAttributes;
  }

  public void setFlattenAttributes(boolean flattenAttrs) {
    this.flattenAttributes = flattenAttrs;
  }
}
