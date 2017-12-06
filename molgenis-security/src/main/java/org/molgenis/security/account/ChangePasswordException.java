package org.molgenis.security.account;

import org.molgenis.data.CodedRuntimeException;

public class ChangePasswordException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "A01";

	public ChangePasswordException()
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
