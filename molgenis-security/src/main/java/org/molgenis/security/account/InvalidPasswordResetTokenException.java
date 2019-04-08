package org.molgenis.security.account;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.security.auth.PasswordResetToken;
import org.molgenis.i18n.BadRequestException;

@SuppressWarnings({"squid:MaximumInheritanceDepth"})
class InvalidPasswordResetTokenException extends BadRequestException {
  private static final String ERROR_CODE = "SEC03";
  private final String id;

  InvalidPasswordResetTokenException(PasswordResetToken passwordResetToken) {
    super(ERROR_CODE);
    requireNonNull(passwordResetToken);
    this.id = passwordResetToken.getId();
  }

  @Override
  public String getMessage() {
    return "id:" + id;
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[0];
  }
}
