package org.molgenis.dataexplorer;

import org.molgenis.security.core.Permission;

/** Permission to perform an action on an EntityType report. */
public enum EntityTypeReportPermission implements Permission {
  // @formatter:off
  MANAGE_REPORT("Permission to manage the report for this EntityType"),
  VIEW_REPORT("Permission to view the report for this EntityType");
  // @formatter:on

  private final String defaultDescription;

  EntityTypeReportPermission(String defaultDescription) {
    this.defaultDescription = defaultDescription;
  }

  @Override
  public String getDefaultDescription() {
    return defaultDescription;
  }
}
