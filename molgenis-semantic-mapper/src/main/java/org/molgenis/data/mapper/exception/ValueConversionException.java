package org.molgenis.data.mapper.exception;

import org.molgenis.data.meta.AttributeType;

import java.text.MessageFormat;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class ValueConversionException extends MappingServiceException
{
	private static final String ERROR_CODE = "M03";
	private final transient Object value;
	private final AttributeType attributeType;

	public ValueConversionException(Object value, AttributeType attributeType)
	{
		super(ERROR_CODE);
		this.value = requireNonNull(value);
		this.attributeType = requireNonNull(attributeType);
	}

	public Object getValue()
	{
		return value;
	}

	public AttributeType getAttributeType()
	{
		return attributeType;
	}

	@Override
	public String getMessage()
	{
		return format("value:%s type:%s", value.toString(), attributeType.toString());
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, value.toString(), attributeType.name());
		}).orElseGet(super::getLocalizedMessage);
	}
}
