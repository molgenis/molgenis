package org.molgenis.security.account;

import org.molgenis.data.security.auth.User;

interface PasswordResetTokenRepository {
  /**
   * Generates a password reset token for a user.
   *
   * @throws PasswordResetTokenCreationException if user is inactive
   */
  String createToken(User user);

  /**
   * Validates the password reset token for a user.
   *
   * @throws UnknownPasswordResetTokenException if token is unknown
   * @throws ExpiredPasswordResetTokenException if token expired
   * @throws InvalidPasswordResetTokenException if token is invalid
   */
  void validateToken(User user, String token);

  /**
   * Deletes the password reset token for a user.
   *
   * @throws UnknownPasswordResetTokenException if token is unknown
   * @throws InvalidPasswordResetTokenException if token is invalid
   */
  void deleteToken(User user, String token);
}
