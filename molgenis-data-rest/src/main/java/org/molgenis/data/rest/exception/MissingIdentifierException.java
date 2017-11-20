package org.molgenis.data.rest.exception;

import org.molgenis.data.CodedRuntimeException;

import java.text.MessageFormat;

import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class MissingIdentifierException extends CodedRuntimeException
{
	private final static String ERROR_CODE = "R09";
	private int count;

	public MissingIdentifierException(int count)
	{
		super(ERROR_CODE);
		this.count = count;
	}

	@Override
	public String getMessage()
	{
		return String.format("index:%s", count);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, count);
		}).orElse(super.getLocalizedMessage());
	}
}