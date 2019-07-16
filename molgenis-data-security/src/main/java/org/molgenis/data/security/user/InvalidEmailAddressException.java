package org.molgenis.data.security.user;

import org.molgenis.util.exception.CodedRuntimeException;

public class InvalidEmailAddressException extends CodedRuntimeException {
  private static final String ERROR_CODE = "DS19";

  public InvalidEmailAddressException() {
    super(ERROR_CODE);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[0];
  }
}
