package org.molgenis.security.account;

import org.molgenis.data.security.user.InvalidEmailAddressException;
import org.molgenis.data.security.user.UnknownUserException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;

interface PasswordResetter {
  /**
   * Sends an email to the user with the given email address containing a password reset link.
   *
   * @throws InvalidEmailAddressException is no user exists for the email address
   * @throws PasswordResetTokenCreationException if user is inactive
   */
  void resetPassword(String emailAddress);

  /**
   * Validate the password reset token for a user.
   *
   * @throws UnknownPasswordResetTokenException if token is unknown
   * @throws ExpiredPasswordResetTokenException if token expired
   * @throws InvalidPasswordResetTokenException if token is invalid
   * @throws UnknownUserException if no user exists for the given username
   */
  void validatePasswordResetToken(String username, String token);

  /**
   * Change the password of a given user using a password reset token.
   *
   * @throws ExpiredPasswordResetTokenException if token expired
   * @throws InvalidPasswordResetTokenException if token is unknown or invalid
   * @throws UnknownUserException if no user exists for the given username
   */
  void changePassword(String username, String token, String password);

  /**
   * Changes the password of the currently authenticated user.
   *
   * @throws AuthenticationCredentialsNotFoundException if no user is authenticated
   */
  void changePasswordAuthenticatedUser(String password);
}
