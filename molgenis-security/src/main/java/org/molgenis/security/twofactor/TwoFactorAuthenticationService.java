package org.molgenis.security.twofactor;

import org.molgenis.security.twofactor.exceptions.InvalidVerificationCodeException;
import org.molgenis.security.twofactor.exceptions.TooManyLoginAttemptsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * <p>Service to use in {@link TwoFactorAuthenticationFilter} and {@link TwoFactorAuthenticationController}</p>
 */
public interface TwoFactorAuthenticationService
{
	/**
	 * <p>Check verificationcode given by user from Google Authenticator</p>
	 *
	 * @param verificationCode code given by user from Google Authenticator
	 * @return is verificationCode valid
	 */
	boolean isVerificationCodeValidForUser(String verificationCode)
			throws InvalidVerificationCodeException, TooManyLoginAttemptsException;

	/**
	 * @return user had too many authentication failures
	 */
	boolean userIsBlocked();

	/**
	 * <p>Add generated userSecret to userdata.</p>
	 *
	 * @param secret given secret for user
	 */
	void saveSecretForUser(String secret) throws UsernameNotFoundException;

	/**
	 * <p>Disable 2 factor authentication for the current user</p>
	 * <p>
	 * Removes the secret key and set the TwoFactorAuthentication field to false
	 */
	void disableForUser();

	/**
	 * <p>Check if the user is 2 factor authentication ready.</p>
	 *
	 * @return is configured for user
	 */
	boolean isConfiguredForUser() throws InternalAuthenticationServiceException;

	/**
	 * @return the secretkey for logged in user
	 */
	String generateSecretKey();
}