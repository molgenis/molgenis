package org.molgenis.oneclickimporter.exceptions;

import org.apache.poi.ss.usermodel.CellType;
import org.molgenis.data.CodedRuntimeException;

import static java.util.Objects.requireNonNull;

public class UnsupportedHeaderCellType extends CodedRuntimeException
{
	private static final String ERROR_CODE = "OCI05";
	private final CellType cellType;

	public UnsupportedHeaderCellType(CellType cellType)
	{
		super(ERROR_CODE);
		this.cellType = requireNonNull(cellType);
	}

	@Override
	public String getMessage()
	{
		return String.format("cellType:%s", cellType.name());
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { cellType.name() };
	}
}
