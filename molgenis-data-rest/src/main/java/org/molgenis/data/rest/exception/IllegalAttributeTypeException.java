package org.molgenis.data.rest.exception;

import org.molgenis.data.meta.AttributeType;

import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

/**
 * thrown of a operation is attempted on a attribute type on which this operation is not supported
 */
public class IllegalAttributeTypeException extends RestApiException
{
	private final static String ERROR_CODE = "R08";
	private AttributeType attributeType;

	public IllegalAttributeTypeException(AttributeType attributeType)
	{
		super(ERROR_CODE);
		this.attributeType = requireNonNull(attributeType);
	}

	public AttributeType getAttributeType()
	{
		return attributeType;
	}

	@Override
	public String getMessage()
	{
		return String.format("attributeType:%s", attributeType.name());
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, attributeType.name());
		}).orElseGet(super::getLocalizedMessage);
	}
}
