package org.molgenis.security.audit;

public enum DataAuditSetting {
  NONE("None"),
  TAGGED("Tagged"),
  ALL("All");

  private final String label;

  DataAuditSetting(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }

  public static DataAuditSetting fromLabel(String label) {
    return valueOf(label.toUpperCase());
  }
}
