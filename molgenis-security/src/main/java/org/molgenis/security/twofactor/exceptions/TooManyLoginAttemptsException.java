package org.molgenis.security.twofactor.exceptions;

import org.springframework.security.authentication.InternalAuthenticationServiceException;

@Deprecated // FIXME extend from CodedRuntimeException
public class TooManyLoginAttemptsException extends InternalAuthenticationServiceException
{
	public TooManyLoginAttemptsException(String message)
	{
		super(message);
	}
}
