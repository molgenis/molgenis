package org.molgenis.oneclickimporter.exceptions;

import org.molgenis.data.CodedRuntimeException;

import static java.util.Objects.requireNonNull;

public class MissingDataException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "OCI02";
	private final String sheettype;
	private final String filename;

	public MissingDataException(String sheettype, String filename)
	{
		super(ERROR_CODE);
		this.sheettype = requireNonNull(sheettype);
		this.filename = requireNonNull(filename);
	}

	@Override
	public String getMessage()
	{
		return String.format("sheettype:%s filename:%s", sheettype, filename);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { sheettype, filename };
	}
}
