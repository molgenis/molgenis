package org.molgenis.security.twofactor.service;

import org.molgenis.security.twofactor.TwoFactorAuthenticationController;
import org.molgenis.security.twofactor.auth.TwoFactorAuthenticationFilter;

/**
 * Service to use in {@link TwoFactorAuthenticationFilter} and {@link
 * TwoFactorAuthenticationController}
 */
public interface TwoFactorAuthenticationService {
  /**
   * Check verificationcode given by user from Google Authenticator
   *
   * @param verificationCode code given by user from Google Authenticator
   * @return is verificationCode valid
   */
  boolean isVerificationCodeValidForUser(String verificationCode);

  /** @return user had too many authentication failures */
  boolean userIsBlocked();

  /**
   * Add generated userSecret to userdata.
   *
   * @param secret given secret for user
   */
  void saveSecretForUser(String secret);

  void resetSecretForUser();

  void enableForUser();

  /**
   * Disable 2 factor authentication for the current user
   *
   * <p>Removes the secret key and set the TwoFactorAuthentication field to false
   */
  void disableForUser();

  /**
   * Check if the user is 2 factor authentication ready.
   *
   * @return is configured for user
   */
  boolean isConfiguredForUser();

  /** @return the secretkey for logged in user */
  String generateSecretKey();
}
