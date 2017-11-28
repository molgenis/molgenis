package org.molgenis.data;

import org.molgenis.data.meta.model.Attribute;

import java.text.MessageFormat;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class DateTimeParseException extends CodedRuntimeException
{
	private final static String ERROR_CODE = "D12";
	private Attribute attribute;
	private String value;

	public DateTimeParseException(Attribute attribute, String value)
	{
		super(ERROR_CODE);
		this.attribute = requireNonNull(attribute);
		this.value = requireNonNull(value);
	}

	public Attribute getAttribute()
	{
		return attribute;
	}

	public String getValue()
	{
		return value;
	}

	@Override
	public String getMessage()
	{
		return String.format("attribute:%s value:%s", attribute.getName(), value);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, attribute.getName(), value);
		}).orElseGet(super::getLocalizedMessage);
	}

}
