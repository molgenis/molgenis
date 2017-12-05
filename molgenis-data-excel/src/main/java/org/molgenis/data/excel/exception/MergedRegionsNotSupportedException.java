package org.molgenis.data.excel.exception;

import org.apache.poi.ss.usermodel.Sheet;
import org.molgenis.data.CodedRuntimeException;

import static java.util.Objects.requireNonNull;

public class MergedRegionsNotSupportedException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "XLS01";
	private Sheet sheet;

	public MergedRegionsNotSupportedException(Sheet sheet)
	{
		super(ERROR_CODE);
		this.sheet = requireNonNull(sheet);
	}
}
