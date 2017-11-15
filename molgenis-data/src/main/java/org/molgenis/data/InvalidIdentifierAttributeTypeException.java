package org.molgenis.data;

import org.molgenis.data.meta.AttributeType;

import java.text.MessageFormat;

import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class InvalidIdentifierAttributeTypeException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "D09";
	private final AttributeType type;

	public InvalidIdentifierAttributeTypeException(AttributeType type)
	{
		super(ERROR_CODE);
		this.type = type;
	}

	@Override
	public String getMessage()
	{
		return String.format("attributeType: ", type.name());
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, type.name());
		}).orElse(super.getLocalizedMessage());
	}
}
