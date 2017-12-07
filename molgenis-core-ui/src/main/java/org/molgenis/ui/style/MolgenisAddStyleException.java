package org.molgenis.ui.style;

import org.molgenis.data.CodedRuntimeException;

import static java.util.Objects.requireNonNull;

public class MolgenisAddStyleException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "C09";
	private final String identifier;
	private final Throwable cause;

	public MolgenisAddStyleException(String identifier, Throwable cause)
	{
		super(ERROR_CODE, cause);
		this.identifier = requireNonNull(identifier);
		this.cause = requireNonNull(cause);
	}

	@Override
	public String getMessage()
	{
		return String.format("identifier:%s cause:%s", identifier, cause.getMessage());
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { identifier };
	}
}
