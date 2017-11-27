package org.molgenis.data.rest.exception;

import org.molgenis.data.meta.model.Attribute;

import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

/**
 * thrown if a conversion form one attribute type to another was attempted but there was a value that was not suitable for the new type
 */
public class IncompatibleValueTypeException extends RestApiException
{
	private final static String ERROR_CODE = "R01";
	private Attribute attribute;
	private String type;
	private String[] expectedTypes;

	public IncompatibleValueTypeException(Attribute attribute, String type, String[] expectedTypes)
	{
		super(ERROR_CODE);
		this.attribute = requireNonNull(attribute);
		this.type = requireNonNull(type);
		this.expectedTypes = requireNonNull(expectedTypes);
	}

	@Override
	public String getMessage()
	{
		return String.format("attribute:%s type:%s, expectedTypes:%s", attribute.getName(), type,
				String.join(",", expectedTypes));
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, attribute.getName(), type, String.join(",", expectedTypes));
		}).orElse(super.getLocalizedMessage());
	}
}
