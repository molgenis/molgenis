package org.molgenis.security.account;

@Deprecated // FIXME extend from CodedRuntimeException
public class UsernameAlreadyExistsException extends Exception
{
	private static final long serialVersionUID = 1L;

	public UsernameAlreadyExistsException()
	{
	}

	public UsernameAlreadyExistsException(String message)
	{
		super(message);
	}

	public UsernameAlreadyExistsException(Throwable cause)
	{
		super(cause);
	}

	public UsernameAlreadyExistsException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public UsernameAlreadyExistsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
