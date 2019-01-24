package org.molgenis.data.security.auth;

import static java.util.Objects.requireNonNull;

import org.molgenis.security.core.Permission;

public enum GroupPermission implements Permission {
  ADD_MEMBERSHIP("Permission to add a member to this group"),
  UPDATE_MEMBERSHIP("Permission to update group membership"),
  REMOVE_MEMBERSHIP("Permission to remove a group member"),
  VIEW_MEMBERSHIP("Permission to view group membership"),
  VIEW("Permission to view group information");

  private String defaultDescription;

  GroupPermission(String defaultDescription) {
    this.defaultDescription = requireNonNull(defaultDescription);
  }

  @Override
  public String getDefaultDescription() {
    return defaultDescription;
  }
}
