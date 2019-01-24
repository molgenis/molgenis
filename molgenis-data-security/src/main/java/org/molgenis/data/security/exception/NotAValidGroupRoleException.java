package org.molgenis.data.security.exception;

import org.molgenis.data.security.auth.Group;
import org.molgenis.data.security.auth.Role;
import org.molgenis.i18n.CodedRuntimeException;

public class NotAValidGroupRoleException extends CodedRuntimeException {
  private static final String ERROR_CODE = "DS15";

  private final transient Role role;
  private final transient Group group;

  public NotAValidGroupRoleException(Role role, Group group) {
    super(ERROR_CODE);
    this.role = role;
    this.group = group;
  }

  @Override
  public String getMessage() {
    return String.format("role:%s group:%s", role.getName(), group.getName());
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {role.getName(), group.getName()};
  }
}
