package org.molgenis.data.rest.exception;

import org.molgenis.data.CodedRuntimeException;

import java.text.MessageFormat;

import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class LoginMethodDisabledException extends CodedRuntimeException
{
	private final static String ERROR_CODE = "R04";

	public LoginMethodDisabledException()
	{
		super(ERROR_CODE);
	}

	@Override
	public String getMessage()
	{
		return "";
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, null, null, null);
		}).orElse(super.getLocalizedMessage());
	}
}
