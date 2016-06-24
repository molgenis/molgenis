package org.molgenis.js;

public class MolgenisJsException extends RuntimeException
{

	private static final long serialVersionUID = 1L;

	public MolgenisJsException()
	{
	}

	public MolgenisJsException(String message)
	{
		super(message);
	}

	public MolgenisJsException(Throwable cause)
	{
		super(cause);
	}

	public MolgenisJsException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public MolgenisJsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
