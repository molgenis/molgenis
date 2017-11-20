package org.molgenis.security.account;

import org.molgenis.data.rest.exception.RestApiException;

import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class InvalidSortOrderException extends RestApiException
{
	private final static String ERROR_CODE = "R06";

	public InvalidSortOrderException()
	{
		super(ERROR_CODE);
	}

	@Override
	public String getMessage()
	{
		return String.format("");
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return format;
		}).orElse(super.getLocalizedMessage());
	}
}
