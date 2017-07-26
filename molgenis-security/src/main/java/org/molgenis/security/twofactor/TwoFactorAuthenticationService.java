package org.molgenis.security.twofactor;

import org.springframework.security.authentication.BadCredentialsException;
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
	void set2FaSecret(String secret) throws UsernameNotFoundException;

	/**
	 *
	 * <p>Check if the user is 2 factor authentication ready.</p>
	 *
	 * @return is ready
	 */
	boolean is2FAEnabledForUser() throws UsernameNotFoundException;

	/**
	 *
	 * <p>Set the new Authentication object</p>
	 *
	 */
	void set2FAAuthenticated() throws BadCredentialsException;








}
