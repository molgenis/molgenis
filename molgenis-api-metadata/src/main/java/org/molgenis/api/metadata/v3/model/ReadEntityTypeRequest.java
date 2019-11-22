package org.molgenis.api.metadata.v3.model;

public class ReadEntityTypeRequest {
  private boolean flattenAttrs;
  private boolean i18n;

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
