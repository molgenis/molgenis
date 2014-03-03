package org.molgenis.data.rest;

import org.molgenis.data.MolgenisDataException;

public class UnknownAttributeException extends MolgenisDataException
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
}
