package org.molgenis.data;

import org.molgenis.data.CodedRuntimeException;
import org.molgenis.data.meta.model.Attribute;

import java.text.MessageFormat;

import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class DataTimeParseException extends CodedRuntimeException
{
	private final static String ERROR_CODE = "D12";
	private Attribute attribute;
	private String value;

	public DataTimeParseException(Attribute attribute, String value)
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
