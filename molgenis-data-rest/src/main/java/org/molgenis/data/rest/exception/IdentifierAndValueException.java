package org.molgenis.data.rest.exception;

import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

/**
 * thrown where we do expect a key value pair (an identifier and a value) but got more than 2 arguments
 */
public class IdentifierAndValueException extends RestApiException
{
	private final static String ERROR_CODE = "R10";

	public IdentifierAndValueException()
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
			return format;
		}).orElseGet(super::getLocalizedMessage);
	}
}