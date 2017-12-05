package org.molgenis.data.excel.exception;

import org.apache.poi.ss.usermodel.CellType;
import org.molgenis.data.CodedRuntimeException;

import static java.util.Objects.requireNonNull;

public class UnsupportedCellTypeException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "XLS04";
	private CellType cellTypeEnum;

	public UnsupportedCellTypeException(CellType cellTypeEnum)
	{
		super(ERROR_CODE);
		this.cellTypeEnum = requireNonNull(cellTypeEnum);
	}
}
