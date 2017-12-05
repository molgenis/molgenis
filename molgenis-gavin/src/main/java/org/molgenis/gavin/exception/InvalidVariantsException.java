package org.molgenis.gavin.exception;

import org.molgenis.data.CodedRuntimeException;

public class InvalidVariantsException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "G03";

	public InvalidVariantsException()
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
