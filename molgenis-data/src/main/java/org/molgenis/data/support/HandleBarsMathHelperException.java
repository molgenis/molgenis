package org.molgenis.data.support;

import org.molgenis.util.exception.CodedRuntimeException;

public class HandleBarsMathHelperException extends CodedRuntimeException {
  private static final String ERROR_CODE = "D12f";

  private final String errorMessage;

  HandleBarsMathHelperException(String errorMessage) {
    super(ERROR_CODE);
    this.errorMessage = errorMessage;
  }

  @Override
  public String getMessage() {
    return String.format("error: %s",errorMessage);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {errorMessage};
  }
}
