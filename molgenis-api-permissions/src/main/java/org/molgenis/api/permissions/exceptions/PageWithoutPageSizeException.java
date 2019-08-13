package org.molgenis.api.permissions.exceptions;

import org.molgenis.util.exception.CodedRuntimeException;

public class PageWithoutPageSizeException extends CodedRuntimeException {
  private static final String ERROR_CODE = "PRM03";

  public PageWithoutPageSizeException() {
    super(ERROR_CODE);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[0];
  }
}
