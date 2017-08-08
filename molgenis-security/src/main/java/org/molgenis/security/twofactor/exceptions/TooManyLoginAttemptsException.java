package org.molgenis.security.twofactor.exceptions;

import org.springframework.security.authentication.InternalAuthenticationServiceException;

/**
 * Created by sido on 02/08/2017.
 */
public class TooManyLoginAttemptsException extends InternalAuthenticationServiceException
{
	public TooManyLoginAttemptsException(String message)
	{
		super(message);
	}

}
