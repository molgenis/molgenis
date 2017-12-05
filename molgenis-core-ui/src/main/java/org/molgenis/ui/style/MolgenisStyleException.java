package org.molgenis.ui.style;

import org.molgenis.data.CodedRuntimeException;

import static java.util.Objects.requireNonNull;

public class MolgenisStyleException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "C05";
	private final Throwable cause;

	public MolgenisStyleException(Throwable cause)
	{
		super(ERROR_CODE, cause);
		this.cause = requireNonNull(cause);
	}

	@Override
	public String getMessage()
	{
		return String.format("cause:%s", cause.getMessage());
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[0];
	}
}
