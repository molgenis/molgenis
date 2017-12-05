package org.molgenis.oneclickimporter.exceptions;

import org.molgenis.data.CodedRuntimeException;

import static java.util.Objects.requireNonNull;

public class EmptySheetException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "OCI01";
	private final String sheettype;
	private final String fileName;

	public EmptySheetException(String sheettype, String fileName)
	{
		super(ERROR_CODE);
		this.sheettype = requireNonNull(sheettype);
		this.fileName = requireNonNull(fileName);
	}

	@Override
	public String getMessage()
	{
		return String.format("sheettype:%s fileName:%s", sheettype, fileName);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { sheettype, fileName };
	}
}
