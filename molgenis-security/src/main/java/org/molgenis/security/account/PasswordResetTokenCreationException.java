package org.molgenis.security.account;

import org.molgenis.util.exception.BadRequestException;

@SuppressWarnings({"java:S110"})
class PasswordResetTokenCreationException extends BadRequestException {
  private static final String ERROR_CODE = "SEC01";

  PasswordResetTokenCreationException() {
    super(ERROR_CODE);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[0];
  }
}
