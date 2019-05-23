package org.molgenis.data.security.exception;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.DataAlreadyExistsException;

public class AclClassAlreadyExistsException extends DataAlreadyExistsException {
  private static final String ERROR_CODE = "DS29";

  private final String typeId;

  public AclClassAlreadyExistsException(String typeId) {
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
