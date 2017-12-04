package org.molgenis.oneclickimporter.exceptions;

import org.molgenis.data.CodedRuntimeException;
import org.molgenis.oneclickimporter.SheetType;

import java.text.MessageFormat;

import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class MissingDataException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "OCI02";
	private SheetType sheettype;
	private String filename;

	public MissingDataException(SheetType sheettype, String filename)
	{
		super(ERROR_CODE);
		this.sheettype = sheettype;
		this.filename = filename;
	}

	@Override
	public String getMessage()
	{
		return String.format("sheettype:%s filename:%s", sheettype, filename);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String label = sheettype == SheetType.EXCELSHEET ? "Excel sheet" : "CSV file";
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, label, filename);
		}).orElse(super.getLocalizedMessage());
	}
}
