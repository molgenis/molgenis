package org.molgenis.data.annotation.core.exception;

import org.molgenis.i18n.CodedRuntimeException;

public class AnnotatorUpdateNotSupportedException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "AN01";

	public AnnotatorUpdateNotSupportedException()
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
