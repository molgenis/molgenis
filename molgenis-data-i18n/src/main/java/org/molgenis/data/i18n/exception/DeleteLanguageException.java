package org.molgenis.data.i18n.exception;

import org.molgenis.data.CodedRuntimeException;

public class DeleteLanguageException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "L02";

	public DeleteLanguageException()
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