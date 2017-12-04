package org.molgenis.data.annotation.core.exception;

import org.molgenis.data.CodedRuntimeException;

import java.text.MessageFormat;

import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class UnsupportedQueryException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "AN04";
	private String field;

	public UnsupportedQueryException(String field)
	{
		super(ERROR_CODE);
		this.field = field;
	}

	@Override
	public String getMessage()
	{
		return String.format("field:%s", field);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, field);
		}).orElse(super.getLocalizedMessage());
	}
}
