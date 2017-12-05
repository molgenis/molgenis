package org.molgenis.oneclickimporter.exceptions;

import org.molgenis.data.CodedRuntimeException;

public class UnknownFileTypeException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "OCI06";

	public UnknownFileTypeException()
	{
		super(ERROR_CODE);
	}

	@Override
	public String getMessage()
	{
		return "";
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[0];
	}
}
