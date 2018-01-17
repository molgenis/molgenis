package org.molgenis.gavin.exception;

import org.molgenis.i18n.CodedRuntimeException;

/**
 * Thrown when no valid variants were found in an input file.
 */
public class NoValidVariantsException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "G03";

	public NoValidVariantsException()
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
