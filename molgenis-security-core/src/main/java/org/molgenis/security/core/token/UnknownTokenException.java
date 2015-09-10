package org.molgenis.security.core.token;

import org.springframework.security.core.AuthenticationException;

/**
 * Exception thrown when an invalid token is encountered
 */
public class UnknownTokenException extends AuthenticationException
{
	private static final long serialVersionUID = -9049083620238941432L;

	public UnknownTokenException(String msg)
	{
		super(msg);
	}

	public UnknownTokenException(String msg, Throwable t)
	{
		super(msg, t);
	}

}
