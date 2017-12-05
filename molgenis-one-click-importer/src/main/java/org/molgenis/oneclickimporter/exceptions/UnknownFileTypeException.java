package org.molgenis.oneclickimporter.exceptions;

import org.molgenis.data.CodedRuntimeException;

import static java.util.Objects.requireNonNull;

public class UnknownFileTypeException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "OCI06";
	private final String extension;

	public UnknownFileTypeException(String extension)
	{
		super(ERROR_CODE);
		this.extension = requireNonNull(extension);
	}

	@Override
	public String getMessage()
	{
		return String.format("extension:%s", extension);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { extension };
	}
}
