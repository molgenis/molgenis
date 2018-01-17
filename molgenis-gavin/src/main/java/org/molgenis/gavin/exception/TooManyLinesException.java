package org.molgenis.gavin.exception;

import org.molgenis.i18n.CodedRuntimeException;

/**
 * Thrown when input file contains too many lines.
 */
public class TooManyLinesException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "G01";
	private final int maxLines;

	public TooManyLinesException(int maxLines)
	{
		super(ERROR_CODE);

		this.maxLines = maxLines;
	}

	@Override
	public String getMessage()
	{
		return String.format("max_lines:%d", maxLines);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { maxLines };
	}
}
