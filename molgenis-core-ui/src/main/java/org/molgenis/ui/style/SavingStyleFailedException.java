package org.molgenis.ui.style;

import org.molgenis.data.CodedRuntimeException;

import static java.util.Objects.requireNonNull;

public class SavingStyleFailedException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "C09";

	private final String fileName;
	private final Throwable cause;

	public SavingStyleFailedException(String fileName, Throwable cause)
	{
		super(ERROR_CODE, cause);
		this.fileName = requireNonNull(fileName);
		this.cause = requireNonNull(cause);
	}

	@Override
	public String getMessage()
	{
		return String.format("fileName:%s cause:%s", fileName, cause.getMessage());
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { fileName };
	}
}
