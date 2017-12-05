package org.molgenis.security.account;

import org.molgenis.data.CodedRuntimeException;

public class UnknownUsernamePasswordException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "A02";

	public UnknownUsernamePasswordException()
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
		return new Object[] {};
	}
}
