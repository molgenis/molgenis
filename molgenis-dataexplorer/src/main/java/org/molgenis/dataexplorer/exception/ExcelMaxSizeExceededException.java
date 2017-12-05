package org.molgenis.dataexplorer.exception;

import org.molgenis.data.CodedRuntimeException;

public class ExcelMaxSizeExceededException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "DE02";
	private final long size;

	public ExcelMaxSizeExceededException(long size)
	{
		super(ERROR_CODE);
		this.size = size;
	}

	public long getSize()
	{
		return size;
	}

	@Override
	public String getMessage()
	{
		return String.format("size:%s", size);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { size };
	}
}
