package org.molgenis.ui.style;

import org.molgenis.i18n.CodedRuntimeException;

import static java.util.Objects.requireNonNull;

public class MolgenisThemeException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "C05";

	public MolgenisThemeException(Throwable cause)
	{
		super(ERROR_CODE, cause);
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
