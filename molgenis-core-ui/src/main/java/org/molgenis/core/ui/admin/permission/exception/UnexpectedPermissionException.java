package org.molgenis.core.ui.admin.permission.exception;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import org.springframework.security.acls.model.Permission;

/**
 * Exception that is thrown in the default section of a switch statement as a defensive programming
 * strategy.
 */
public class UnexpectedPermissionException extends RuntimeException {
  private static final String UNEXPECTED_PERMISSION_FORMAT = "Illegal permission '%s'";

  private final Permission permission;

  public <E extends Enum> UnexpectedPermissionException(Permission permission) {
    this.permission = requireNonNull(permission);
  }

  @Override
  public String getMessage() {
    return format(UNEXPECTED_PERMISSION_FORMAT, permission);
  }
}
