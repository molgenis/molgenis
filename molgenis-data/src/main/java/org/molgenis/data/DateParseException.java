package org.molgenis.data;

import org.molgenis.data.meta.model.Attribute;

import java.text.MessageFormat;

import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class DateParseException extends CodedRuntimeException
{
	private final static String ERROR_CODE = "D11";
	private Attribute attribute;
	private String value;

	public DateParseException(Attribute attribute, String value)
	{
		super(ERROR_CODE);
		this.attribute = attribute;
		this.value = value;
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
		}).orElse(super.getLocalizedMessage());
	}
}
