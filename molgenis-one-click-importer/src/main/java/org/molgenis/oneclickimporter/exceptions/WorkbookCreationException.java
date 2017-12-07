package org.molgenis.oneclickimporter.exceptions;

import org.molgenis.data.CodedRuntimeException;

public class WorkbookCreationException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "OCI04";

	public WorkbookCreationException()
	{
		super(ERROR_CODE);
	}

	@Override
	public String getMessage()
	{
		return "";
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[0];
	}
}
