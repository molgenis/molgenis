package org.molgenis.app.manager.exception;

import org.molgenis.util.exception.CodedRuntimeException;

public class InvalidAppConfigException extends CodedRuntimeException {
  private static final String ERROR_CODE = "AM04";

  public InvalidAppConfigException() {
    super(ERROR_CODE);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[0];
  }
}
