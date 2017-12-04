package org.molgenis.data.rsql;

import org.molgenis.data.CodedRuntimeException;

import java.text.MessageFormat;

import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class RSQLParseException extends CodedRuntimeException
{
	public static String ERROR_CODE = "C02";
	private String rsql;

	public RSQLParseException(String rsql)
	{
		super(ERROR_CODE);
		this.rsql = rsql;
	}

	@Override
	public String getMessage()
	{
		return String.format("rsql:%s", rsql);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, rsql);
		}).orElse(super.getLocalizedMessage());
	}
}
