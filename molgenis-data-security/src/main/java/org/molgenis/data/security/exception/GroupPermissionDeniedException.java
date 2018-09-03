package org.molgenis.data.security.exception;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.security.auth.GroupPermission;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class GroupPermissionDeniedException extends PermissionDeniedException {
  public static final String ERROR_CODE = "DS10";

  private final GroupPermission permission;
  private final String groupName;

  public GroupPermissionDeniedException(GroupPermission permission, String groupName) {
    super(ERROR_CODE);
    this.permission = requireNonNull(permission);
    this.groupName = requireNonNull(groupName);
  }

  @Override
  public String getMessage() {
    return String.format("permission:%s groupName:%s", permission, groupName);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {permission.getName(), groupName};
  }
}
