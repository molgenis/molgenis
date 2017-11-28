package org.molgenis.data.importer.emx.exception;

import org.molgenis.data.meta.AttributeType;

import java.text.MessageFormat;

import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class InvalidAttributeTypeException extends EmxException
{
	private final static String ERROR_CODE = "E10";
	private String attributeAttribute;
	private AttributeType attributeType;
	private String attribute;
	private String[] validOptions;
	private int rowIndex;

	public InvalidAttributeTypeException(String attributeAttribute, AttributeType attributeType, String attribute,
			String[] validOptions, int rowIndex)
	{
		super(ERROR_CODE);
		this.attributeAttribute = attributeAttribute;
		this.attributeType = attributeType;
		this.attribute = attribute;
		this.validOptions = validOptions;
		this.rowIndex = rowIndex;
	}

	@Override
	public String getMessage()
	{
		return String.format("attributeAttribute:%s attributeType:%s attribute:%s validOptions:%s rowIndex:%d",
				attributeType, attributeAttribute, attribute, String.join(",", validOptions), rowIndex);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, attributeAttribute, attributeType, attribute, validOptions, rowIndex);
		}).orElse(super.getLocalizedMessage());
	}
}
