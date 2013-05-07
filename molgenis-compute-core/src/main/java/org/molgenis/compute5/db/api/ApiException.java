package org.molgenis.compute5.db.api;

public class ApiException extends Exception
{
	private static final long serialVersionUID = 1L;

	public ApiException()
	{
	}

	public ApiException(String message)
	{
		super(message);
	}

	public ApiException(Throwable t)
	{
		super(t);
	}

	public ApiException(String message, Throwable t)
	{
		super(message, t);
	}

}
