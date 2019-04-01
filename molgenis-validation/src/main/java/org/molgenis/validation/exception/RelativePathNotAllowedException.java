package org.molgenis.validation.exception;

import org.molgenis.i18n.CodedRuntimeException;

public class RelativePathNotAllowedException extends CodedRuntimeException {
  private static final String ERROR_CODE = "V03";

  public RelativePathNotAllowedException() {
    super(ERROR_CODE);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[0];
  }
}
