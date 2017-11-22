package org.molgenis.data.importer.emx.exception;

import java.text.MessageFormat;

import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class UnknownMappedByException extends EmxException
{
	private final static String ERROR_CODE = "E08";
	private String attributeName;
	private String mappedByAttributeName;
	private int rowIndex;

	public UnknownMappedByException(String attributeName, String mappedByAttributeName, int rowIndex)
	{
		super(ERROR_CODE);
		this.attributeName = attributeName;
		this.mappedByAttributeName = mappedByAttributeName;
		this.rowIndex = rowIndex;
	}

	@Override
	public String getMessage()
	{
		return String.format("attributeName:%s referenceName:%s, rowIndex:%d", attributeName, mappedByAttributeName,
				rowIndex);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, mappedByAttributeName, attributeName, rowIndex);
		}).orElse(super.getLocalizedMessage());
	}
}
