package org.molgenis.data.export.exception;

import static java.util.Objects.requireNonNull;

import org.molgenis.i18n.CodedRuntimeException;

public class InvalidEntityIdentifierException extends CodedRuntimeException {
  private static final String ERROR_CODE = "DL03";
  private final String entityTypeId;

  public InvalidEntityIdentifierException(String entityTypeId) {
    super(ERROR_CODE);
    this.entityTypeId = requireNonNull(entityTypeId);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {entityTypeId};
  }
}
