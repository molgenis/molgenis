package org.molgenis.data.rest;

public class UnknownAttributeException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public UnknownAttributeException()
	{
	}

	public UnknownAttributeException(String message)
	{
		super(message);
	}

	public UnknownAttributeException(Throwable cause)
	{
		super(cause);
	}

	public UnknownAttributeException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public UnknownAttributeException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
