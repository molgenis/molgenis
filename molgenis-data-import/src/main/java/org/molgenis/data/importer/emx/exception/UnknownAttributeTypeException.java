package org.molgenis.data.importer.emx.exception;

import java.text.MessageFormat;

import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class UnknownAttributeTypeException extends EmxException
{
	private final static String ERROR_CODE = "E03";
	private String emxDataType;
	private String attributeName;
	private int rowIndex;

	public UnknownAttributeTypeException(String emxDataType, String attributeName, int rowIndex)
	{
		super(ERROR_CODE);
		this.emxDataType = emxDataType;
		this.attributeName = attributeName;
		this.rowIndex = rowIndex;
	}

	@Override
	public String getMessage()
	{
		return String.format("dataType:%s attribute:%s rowIndex:%d", emxDataType, attributeName, rowIndex);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, emxDataType, attributeName, rowIndex);
		}).orElse(super.getLocalizedMessage());
	}
}
