package org.molgenis.security.twofactor;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * @author sido
 * @since 26-7-2017
 */
public interface TwoFactorAuthenticationService
{

	/**
	 *
	 * <p>Check verificationcode given by user from Google Authenticator</p>
	 *
	 * @param verificationCode code given by user from Google Authenticator
	 * @return is verificationCode valid
	 */
	boolean isVerificationCodeValid(String verificationCode) throws UsernameNotFoundException, BadCredentialsException;

	/**
	 *
	 *
	 * <p>Add generated userSecret to userdata.</p>
	 *
	 * @param secret
	 */
	void setSecretKey(String secret) throws UsernameNotFoundException;

	/**
	 *
	 * <p>Has user 2 factor authentication enabled?</p>
	 *
	 * @return is ready
	 */
	boolean isEnabledForUser() throws UsernameNotFoundException;

	/**
	 *
	 * <p>Check if the user is 2 factor authentication ready.</p>
	 *
	 * @return is configured
	 * @throws UsernameNotFoundException
	 */
	boolean isConfiguredForUser() throws UsernameNotFoundException;

	/**
	 *
	 * <p>Get user for QR-code generation</p>
	 *
	 * @return
	 * @throws UsernameNotFoundException
	 */
	String getUnAuthenticatedUser() throws UsernameNotFoundException;

	/**
	 *
	 * <p>Set the new Authentication object</p>
	 *
	 */
	void authenticate() throws BadCredentialsException;








}
