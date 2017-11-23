package org.molgenis.data.rest.exception;

import java.text.MessageFormat;

import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

/**
 * thrown if a loging method was used that is diabled in this MOLGENIS instance
 * for example: the api/v1/login is disabled when 2fa is active on a server
 */
public class LoginMethodDisabledException extends RestApiException
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
