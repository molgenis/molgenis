package org.molgenis.data.importer.emx.exception;

import java.text.MessageFormat;

import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class InvalidAttributeValueException extends EmxException
{
	private final static String ERROR_CODE = "E13";
	private String attribute;
	private String value;
	private String sheetName;
	private String[] allowedTypes;
	private int rowIndex;

	public InvalidAttributeValueException(String attribute, String value, String sheetName, String[] allowedTypes,
			int rowIndex)
	{
		super(ERROR_CODE);
		this.attribute = attribute;
		this.value = value;
		this.sheetName = sheetName;
		this.allowedTypes = allowedTypes;
		this.rowIndex = rowIndex;
	}

	@Override
	public String getMessage()
	{
		return String.format("attribute:%s value:%s sheetName:%s allowedTypes:%s rowIndex:%d", attribute, value,
				sheetName, allowedTypes, rowIndex);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, attribute, value, sheetName, String.join(",", allowedTypes), rowIndex);
		}).orElse(super.getLocalizedMessage());
	}
}
