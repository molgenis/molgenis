package org.molgenis.python;

public class MolgenisPythonException extends RuntimeException
{
	private static final long serialVersionUID = 4675578564750997809L;

	public MolgenisPythonException()
	{
	}

	public MolgenisPythonException(String message)
	{
		super(message);
	}

	public MolgenisPythonException(Throwable cause)
	{
		super(cause);
	}

	public MolgenisPythonException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
