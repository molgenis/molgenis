package org.molgenis.security.twofactor;

import org.springframework.security.authentication.InternalAuthenticationServiceException;

/**
 * This exception is used to determine in the {@link TwoFactorAuthenticationController} what message should be thrown
 */
public class InvalidVerificationCodeException extends InternalAuthenticationServiceException
{
	public InvalidVerificationCodeException(String message)
	{
		super(message);
	}
}
