package org.molgenis.data.importer.emx.exception;

import java.text.MessageFormat;

import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class InvalidBoolAttributeValueException extends EmxException
{
	private final static String ERROR_CODE = "E02";
	private String attributeAttributeName;
	private String booleanString;
	private String sheetName;
	private int rowIndex;

	public InvalidBoolAttributeValueException(String attributeAttributeName, String booleanString, String sheetName,
			int rowIndex)
	{
		super(ERROR_CODE);
		this.attributeAttributeName = attributeAttributeName;
		this.booleanString = booleanString;
		this.sheetName = sheetName;
		this.rowIndex = rowIndex;
	}

	@Override
	public String getMessage()
	{
		return String.format("attributeAttributeName:%s booleanString:%s sheetName:%s rowIndex:%d",
				attributeAttributeName, booleanString, sheetName, rowIndex);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, attributeAttributeName, booleanString, sheetName, rowIndex);
		}).orElse(super.getLocalizedMessage());
	}
}
