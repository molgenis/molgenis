package org.molgenis.ui.style;

import org.molgenis.data.CodedRuntimeException;

public class CannotDeleteCurrentThemeException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "C04";

	public CannotDeleteCurrentThemeException()
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
