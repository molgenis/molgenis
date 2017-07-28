package org.molgenis.security.twofactor;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * <p>Service to use in {@link TwoFactorAuthenticationFilter} and {@link TwoFactorAuthenticationController}</p>
 *
 * @author sido
 * @since 26-7-2017
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
			throws UsernameNotFoundException, BadCredentialsException;

	/**
	 * <p>Add generated userSecret to userdata.</p>
	 *
	 * @param secret given secret for user
	 */
	void setSecretKey(String secret) throws UsernameNotFoundException;

	/**
	 * <p>Has user 2 factor authentication enabled?</p>
	 *
	 * @return is ready
	 */
	boolean isEnabledForUser() throws UsernameNotFoundException;

	/**
	 * <p>Check if the user is 2 factor authentication ready.</p>
	 *
	 * @return is configured for user
	 */
	boolean isConfiguredForUser() throws UsernameNotFoundException;

	/**
	 * <p>Set the new Authentication object</p>
	 */
	void authenticate() throws BadCredentialsException;

	/**
	 * @return the secretkey for logged in user
	 */
	String generateSecretKey();

}
