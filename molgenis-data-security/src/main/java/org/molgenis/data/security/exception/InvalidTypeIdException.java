package org.molgenis.data.security.exception;

import static java.util.Objects.requireNonNull;

import org.molgenis.i18n.BadRequestException;

public class InvalidTypeIdException extends BadRequestException {
  private static final String ERROR_CODE = "DS22";

  private final String typeId;

  public InvalidTypeIdException(String typeId) {
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
