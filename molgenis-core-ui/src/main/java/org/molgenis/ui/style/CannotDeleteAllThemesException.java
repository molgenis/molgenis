package org.molgenis.ui.style;

import org.molgenis.data.CodedRuntimeException;

public class CannotDeleteAllThemesException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "C03";

	public CannotDeleteAllThemesException()
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
