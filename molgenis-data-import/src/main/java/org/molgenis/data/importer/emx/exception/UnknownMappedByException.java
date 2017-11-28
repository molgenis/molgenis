package org.molgenis.data.importer.emx.exception;

import org.molgenis.data.meta.model.Attribute;

import java.text.MessageFormat;

import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class UnknownMappedByException extends EmxException
{
	private final static String ERROR_CODE = "E08";
	private Attribute attribute;
	private String mappedByAttributeName;
	private int rowIndex;

	public UnknownMappedByException(Attribute attribute, String mappedByAttributeName, int rowIndex)
	{
		super(ERROR_CODE);
		this.attribute = attribute;
		this.mappedByAttributeName = mappedByAttributeName;
		this.rowIndex = rowIndex;
	}

	@Override
	public String getMessage()
	{
		return String.format("attributeName:%s referenceName:%s, rowIndex:%d", attribute.getName(),
				mappedByAttributeName,
				rowIndex);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, mappedByAttributeName, attribute.getName(), rowIndex);
		}).orElse(super.getLocalizedMessage());
	}
}
