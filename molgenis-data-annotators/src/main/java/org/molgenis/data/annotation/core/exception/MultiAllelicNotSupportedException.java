package org.molgenis.data.annotation.core.exception;

import org.molgenis.i18n.CodedRuntimeException;

public class MultiAllelicNotSupportedException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "AN02";

	public MultiAllelicNotSupportedException()
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
