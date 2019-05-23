package org.molgenis.api.permissions.exceptions;

import org.molgenis.i18n.CodedRuntimeException;

public class MissingUserOrRoleException extends CodedRuntimeException {
  private static final String ERROR_CODE = "PRM06";

  public MissingUserOrRoleException() {
    super(ERROR_CODE);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[0];
  }
}
