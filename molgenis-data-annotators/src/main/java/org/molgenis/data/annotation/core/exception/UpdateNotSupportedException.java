package org.molgenis.data.annotation.core.exception;

import org.molgenis.data.CodedRuntimeException;

public class UpdateNotSupportedException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "AN01";

	public UpdateNotSupportedException()
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
