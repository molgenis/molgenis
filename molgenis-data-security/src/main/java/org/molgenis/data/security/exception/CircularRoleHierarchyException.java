package org.molgenis.data.security.exception;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import org.molgenis.data.security.auth.Role;
import org.molgenis.util.exception.BadRequestException;

/** Thrown when the 'includes' field of a Role contains a circular Role hierarchy. */
@SuppressWarnings("java:S110")
public class CircularRoleHierarchyException extends BadRequestException {

  private static final String ERROR_CODE = "DS37";
  private final String id;
  private final String name;

  public CircularRoleHierarchyException(Role role) {
    super(ERROR_CODE);
    requireNonNull(role);
    this.name = role.getName();
    this.id = role.getId();
  }

  @Override
  public String getMessage() {
    return format("id:%s,name:%s", id, name);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {name};
  }
}
