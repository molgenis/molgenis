package org.molgenis.gavin.exception;

import org.molgenis.i18n.CodedRuntimeException;

/**
 * Thrown when an input file contains a mix of line types.
 */
public class MixedLineTypesException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "G02";

	public MixedLineTypesException()
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
