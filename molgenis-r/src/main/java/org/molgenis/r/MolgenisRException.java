package org.molgenis.r;

public class MolgenisRException extends RuntimeException
{
	private static final long serialVersionUID = 4675578564750997809L;

	public MolgenisRException()
	{
	}

	public MolgenisRException(String message)
	{
		super(message);
	}

	public MolgenisRException(Throwable cause)
	{
		super(cause);
	}

	public MolgenisRException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
