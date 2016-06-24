package org.molgenis.dataexplorer.controller;

public class InputValidationException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public InputValidationException()
	{
	}

	public InputValidationException(String message)
	{
		super(message);
	}

	public InputValidationException(Throwable cause)
	{
		super(cause);
	}

	public InputValidationException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public InputValidationException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
