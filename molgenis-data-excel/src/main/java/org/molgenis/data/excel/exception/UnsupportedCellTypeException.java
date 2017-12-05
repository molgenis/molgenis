package org.molgenis.data.excel.exception;

import org.apache.poi.ss.usermodel.CellType;
import org.molgenis.data.CodedRuntimeException;

import static java.util.Objects.requireNonNull;

public class UnsupportedCellTypeException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "XLS04";
	private final CellType cellTypeEnum;

	public UnsupportedCellTypeException(CellType cellTypeEnum)
	{
		super(ERROR_CODE);
		this.cellTypeEnum = requireNonNull(cellTypeEnum);
	}

	@Override
	public String getMessage()
	{
		return String.format("cellTypeEnum:%s", cellTypeEnum.name());
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { cellTypeEnum.name() };
	}
}
