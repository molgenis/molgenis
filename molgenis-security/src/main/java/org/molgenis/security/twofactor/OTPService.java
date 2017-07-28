package org.molgenis.security.twofactor;

import org.springframework.security.authentication.BadCredentialsException;

/**
 * @author tommy
 */
public interface OTPService
{

	/**
	 * <p>Use TOTP algorithm to verify validity of secret and verification code.</p>
	 *
	 * @param verificationCode given verificationCode
	 * @param secretKey        generated secret key
	 * @return vericifcationCode matches secret key
	 */
	boolean tryVerificationCode(String verificationCode, String secretKey) throws BadCredentialsException;

}
