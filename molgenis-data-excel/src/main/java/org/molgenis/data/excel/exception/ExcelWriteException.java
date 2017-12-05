package org.molgenis.data.excel.exception;

import org.molgenis.data.CodedRuntimeException;

import java.io.IOException;

public class ExcelWriteException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "XLS03";
	private IOException e;

	public ExcelWriteException(IOException e)
	{
		super(ERROR_CODE, e);
	}
}
