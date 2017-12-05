package org.molgenis.oneclickimporter.exceptions;

import org.molgenis.data.CodedRuntimeException;

import static java.util.Objects.requireNonNull;

public class InconsistentColumnCountException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "OCI03";
	private final String filename;

	public InconsistentColumnCountException(String filename)
	{
		super(ERROR_CODE);
		this.filename = requireNonNull(filename);
	}

	@Override
	public String getMessage()
	{
		return String.format("filename:%s", filename);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { filename };
	}
}
