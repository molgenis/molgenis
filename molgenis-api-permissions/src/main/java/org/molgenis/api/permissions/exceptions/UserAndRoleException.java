package org.molgenis.api.permissions.exceptions;

import org.molgenis.i18n.CodedRuntimeException;

public class UserAndRoleException extends CodedRuntimeException {
  private static final String ERROR_CODE = "PRM05";

  public UserAndRoleException() {
    super(ERROR_CODE);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[0];
  }
}
