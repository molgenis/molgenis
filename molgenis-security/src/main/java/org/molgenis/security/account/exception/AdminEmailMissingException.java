package org.molgenis.security.account.exception;

import org.molgenis.i18n.CodedRuntimeException;

public class AdminEmailMissingException extends CodedRuntimeException {
  private static final String ERROR_CODE = "SEC05";

  public AdminEmailMissingException() {
    super(ERROR_CODE);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[0];
  }
}
