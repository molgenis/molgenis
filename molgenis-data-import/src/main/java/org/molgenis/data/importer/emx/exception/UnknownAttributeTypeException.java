package org.molgenis.data.importer.emx.exception;

import org.molgenis.data.meta.model.Attribute;

import java.text.MessageFormat;

import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class UnknownAttributeTypeException extends EmxException
{
	private final static String ERROR_CODE = "E03";
	private String emxDataType;
	private Attribute attribute;
	private int rowIndex;

	public UnknownAttributeTypeException(String emxDataType, Attribute attribute, int rowIndex)
	{
		super(ERROR_CODE);
		this.emxDataType = emxDataType;
		this.attribute = attribute;
		this.rowIndex = rowIndex;
	}

	@Override
	public String getMessage()
	{
		return String.format("dataType:%s attribute:%s rowIndex:%d", emxDataType, attribute.getName(), rowIndex);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, emxDataType, attribute.getName(), rowIndex);
		}).orElse(super.getLocalizedMessage());
	}
}
