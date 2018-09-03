package org.molgenis.app.manager.exception;

import static java.util.Objects.requireNonNull;

import org.molgenis.i18n.CodedRuntimeException;

public class CouldNotDeleteAppException extends CodedRuntimeException {
  private static final String ERROR_CODE = "AM09";
  private final String id;

  public CouldNotDeleteAppException(String id) {
    super(ERROR_CODE);
    this.id = requireNonNull(id);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {id};
  }

  @Override
  public String getMessage() {
    return id;
  }
}
