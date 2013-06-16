package org.molgenis.omx.converters;

public class ValueConverterException extends Exception
{
	private static final long serialVersionUID = 1L;

	public ValueConverterException(String message)
	{
		super(message);
	}

	public ValueConverterException(Exception exception)
	{
		super(exception);
	}

	public ValueConverterException(String message, Exception exception)
	{
		super(message, exception);
	}
}
