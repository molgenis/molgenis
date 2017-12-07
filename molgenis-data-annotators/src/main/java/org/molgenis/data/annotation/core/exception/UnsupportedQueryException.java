package org.molgenis.data.annotation.core.exception;

import org.molgenis.data.CodedRuntimeException;

import static java.util.Objects.requireNonNull;

public class UnsupportedQueryException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "AN04";
	private final String field;

	public UnsupportedQueryException(String field)
	{
		super(ERROR_CODE);
		this.field = requireNonNull(field);
	}

	@Override
	public String getMessage()
	{
		return String.format("field:%s", field);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { field };
	}
}
