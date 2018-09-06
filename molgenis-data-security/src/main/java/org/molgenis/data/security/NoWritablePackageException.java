package org.molgenis.data.security;

import org.molgenis.i18n.CodedRuntimeException;

public class NoWritablePackageException extends CodedRuntimeException {
  private static final String ERROR_CODE = "DS07";

  public NoWritablePackageException() {
    super(ERROR_CODE);
  }

  @Override
  public String getMessage() {
    return "no writable package";
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[0];
  }
}
