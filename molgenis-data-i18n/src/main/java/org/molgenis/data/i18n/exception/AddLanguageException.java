package org.molgenis.data.i18n.exception;

import org.molgenis.data.CodedRuntimeException;

public class AddLanguageException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "L01";

	public AddLanguageException()
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