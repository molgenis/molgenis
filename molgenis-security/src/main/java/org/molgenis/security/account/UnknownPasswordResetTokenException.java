package org.molgenis.security.account;

import org.molgenis.i18n.BadRequestException;

@SuppressWarnings({"squid:MaximumInheritanceDepth"})
class UnknownPasswordResetTokenException extends BadRequestException {
  private static final String ERROR_CODE = "SEC04";

  UnknownPasswordResetTokenException() {
    super(ERROR_CODE);
  }

  @Override
  public String getMessage() {
    return "token:<secret>";
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[0];
  }
}
