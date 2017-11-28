package org.molgenis.data.semanticsearch.explain.service.exception;

import org.molgenis.data.CodedRuntimeException;

import static java.util.Objects.requireNonNull;

public class TermNotFoundException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "Q01";
	private final String description;

	public TermNotFoundException(String description)
	{
		super(ERROR_CODE);
		this.description = requireNonNull(description);
	}

	public String getDescription()
	{
		return description;
	}

	@Override
	public String getMessage()
	{
		return String.format("description:%s", description);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { description };
	}
}