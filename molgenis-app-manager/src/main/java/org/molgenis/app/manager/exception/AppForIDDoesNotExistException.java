package org.molgenis.app.manager.exception;

import static java.util.Objects.requireNonNull;

import org.molgenis.util.exception.CodedRuntimeException;

public class AppForIDDoesNotExistException extends CodedRuntimeException {
  private static final String ERROR_CODE = "AM05";
  private final String id;

  public AppForIDDoesNotExistException(String id) {
    super(ERROR_CODE);
    this.id = requireNonNull(id);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {id};
  }

  public String getId() {
    return id;
  }
}
