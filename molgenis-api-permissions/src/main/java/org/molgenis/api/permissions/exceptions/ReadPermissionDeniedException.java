package org.molgenis.api.permissions.exceptions;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.security.exception.PermissionDeniedException;

public class ReadPermissionDeniedException extends PermissionDeniedException {
  private static final String ERROR_CODE = "PRM07";

  private final String typeId;

  public ReadPermissionDeniedException(String typeId) {
    super(ERROR_CODE);
    this.typeId = requireNonNull(typeId);
  }

  @Override
  public String getMessage() {
    return String.format("typeId:%s", typeId);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {typeId};
  }
}
