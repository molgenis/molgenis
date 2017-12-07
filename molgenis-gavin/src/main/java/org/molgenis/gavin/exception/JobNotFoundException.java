package org.molgenis.gavin.exception;

import org.molgenis.data.CodedRuntimeException;

public class JobNotFoundException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "G04";

	public JobNotFoundException()
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
