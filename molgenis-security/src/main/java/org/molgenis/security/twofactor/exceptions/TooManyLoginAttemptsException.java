package org.molgenis.security.twofactor.exceptions;

import org.springframework.security.authentication.InternalAuthenticationServiceException;

public class TooManyLoginAttemptsException extends InternalAuthenticationServiceException
{
	public TooManyLoginAttemptsException(String message)
	{
		super(message);
	}
}
