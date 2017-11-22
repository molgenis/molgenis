package org.molgenis.data.importer.emx.exception;

import java.text.MessageFormat;

import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class InvalidCaseException extends EmxException
{
	private final static String ERROR_CODE = "E04";
	private String attributeName;
	private String sheetName;
	private String emxAttrName;

	public InvalidCaseException(String attributeName, String sheetName, String emxAttrName)
	{
		super(ERROR_CODE);
		this.attributeName = attributeName;
		this.sheetName = sheetName;
		this.emxAttrName = emxAttrName;
	}

	@Override
	public String getMessage()
	{
		return String.format("attributeName:%s sheetName:%s emxAttrName: %s", attributeName, sheetName, emxAttrName);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, attributeName, sheetName, emxAttrName);
		}).orElse(super.getLocalizedMessage());
	}

}
