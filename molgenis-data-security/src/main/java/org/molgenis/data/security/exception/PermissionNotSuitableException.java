package org.molgenis.data.security.exception;

import static java.util.Objects.requireNonNull;

import org.molgenis.i18n.BadRequestException;

public class PermissionNotSuitableException extends BadRequestException {
  private static final String ERROR_CODE = "DS23";

  private final String permission;
  private final String typeId;

  public PermissionNotSuitableException(String permission, String typeId) {
    super(ERROR_CODE);
    this.permission = requireNonNull(permission);
    this.typeId = requireNonNull(typeId);
  }

  @Override
  public String getMessage() {
    return String.format("permission:%s, typeId:%s", permission, typeId);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {permission, typeId};
  }
}
