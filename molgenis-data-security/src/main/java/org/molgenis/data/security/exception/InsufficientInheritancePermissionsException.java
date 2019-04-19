package org.molgenis.data.security.exception;

import org.molgenis.i18n.CodedRuntimeException;

public class InsufficientInheritancePermissionsException extends CodedRuntimeException {
  private static final String ERROR_CODE = "PRM13";

  public InsufficientInheritancePermissionsException() {
    super(ERROR_CODE);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {};
  }
}
