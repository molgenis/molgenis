package org.molgenis.api.permissions.exceptions;

import org.molgenis.i18n.CodedRuntimeException;

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
