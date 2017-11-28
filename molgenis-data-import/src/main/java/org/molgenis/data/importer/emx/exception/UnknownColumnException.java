package org.molgenis.data.importer.emx.exception;

import java.text.MessageFormat;

import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class UnknownColumnException extends EmxException
{
	private final static String ERROR_CODE = "E07";
	private String columnName;
	private String sheetName;

	public UnknownColumnException(String columnName, String sheetName)
	{
		super(ERROR_CODE);
		this.columnName = columnName;
		this.sheetName = sheetName;
	}

	@Override
	public String getMessage()
	{
		return String.format("columnName:%s sheet:%s", columnName, sheetName);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, columnName, sheetName);
		}).orElse(super.getLocalizedMessage());
	}
}
