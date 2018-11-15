package org.molgenis.data.export.exception;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import org.molgenis.i18n.CodedRuntimeException;

public class InvalidEmxIdentifierException extends CodedRuntimeException {
  private static final String ERROR_CODE = "EXP03";
  private final String entityTypeId;

  public InvalidEmxIdentifierException(String entityTypeId) {
    super(ERROR_CODE);
    this.entityTypeId = requireNonNull(entityTypeId);
  }

  @Override
  public String getMessage() {
    return format("entityTypeId:%s", entityTypeId);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {entityTypeId};
  }
}
