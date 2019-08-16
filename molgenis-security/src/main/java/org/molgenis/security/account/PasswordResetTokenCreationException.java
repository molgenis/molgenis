package org.molgenis.security.account;

import org.molgenis.util.exception.BadRequestException;

@SuppressWarnings({"squid:MaximumInheritanceDepth"})
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
