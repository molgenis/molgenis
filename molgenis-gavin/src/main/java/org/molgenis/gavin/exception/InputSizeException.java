package org.molgenis.gavin.exception;

import org.molgenis.data.CodedRuntimeException;

public class InputSizeException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "G01";
	private final int maxLines;

	public InputSizeException(int maxLines)
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
