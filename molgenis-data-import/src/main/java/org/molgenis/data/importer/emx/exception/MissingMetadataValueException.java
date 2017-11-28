package org.molgenis.data.importer.emx.exception;

import java.text.MessageFormat;

import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class MissingMetadataValueException extends EmxException
{
	private final static String ERROR_CODE = "E06";
	private String attributeAttributeName;
	private String attributeName;
	private String sheetName;
	private int rowIndex;

	public MissingMetadataValueException(String attributeAttributeName, String attributeName, String sheetName,
			int rowIndex)
	{
		super(ERROR_CODE);
		this.attributeAttributeName = attributeAttributeName;
		this.attributeName = attributeName;
		this.sheetName = sheetName;
		this.rowIndex = rowIndex;
	}

	@Override
	public String getMessage()
	{
		return String.format("attributeAttribute:%s attribute:%s sheetName: %s rowIndex:%d", attributeAttributeName,
				attributeName, sheetName, rowIndex);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, attributeAttributeName, attributeName, sheetName, rowIndex);
		}).orElse(super.getLocalizedMessage());
	}
}
