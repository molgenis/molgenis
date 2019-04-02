package org.molgenis.security.account;

import static java.util.Objects.requireNonNull;

import java.time.Instant;
import org.molgenis.data.security.auth.PasswordResetToken;
import org.molgenis.i18n.BadRequestException;

@SuppressWarnings({"squid:MaximumInheritanceDepth"})
class ExpiredPasswordResetTokenException extends BadRequestException {
  private static final String ERROR_CODE = "SEC02";
  private final String id;
  private final Instant expirationDate;

  ExpiredPasswordResetTokenException(PasswordResetToken passwordResetToken) {
    super(ERROR_CODE);
    requireNonNull(passwordResetToken);
    this.id = passwordResetToken.getId();
    this.expirationDate = passwordResetToken.getExpirationDate();
  }

  @Override
  public String getMessage() {
    return "id:" + id + " expirationDate:" + expirationDate;
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[0];
  }
}
