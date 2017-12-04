package org.molgenis.oneclickimporter.exceptions;

import org.apache.poi.ss.usermodel.CellType;
import org.molgenis.data.CodedRuntimeException;

import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class UnsupportedHeaderCellType extends CodedRuntimeException
{
	private static final String ERROR_CODE = "OCI05";
	private CellType cellType;

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
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, cellType.name());
		}).orElse(super.getLocalizedMessage());
	}
}
