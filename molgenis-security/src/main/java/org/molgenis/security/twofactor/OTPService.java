package org.molgenis.security.twofactor;

import org.springframework.security.authentication.BadCredentialsException;

/**
 * OTPService is used to determine if the {@link UserSecret} matches the given validationcode.
 * <p>
 * The base is the TOTP algorithm. We have included a thirdparty library which serves the code for the validity check
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
