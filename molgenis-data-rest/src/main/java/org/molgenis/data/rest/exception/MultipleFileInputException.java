package org.molgenis.data.rest.exception;

import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class MultipleFileInputException extends RestApiException
{
	private final static String ERROR_CODE = "R07";

	public MultipleFileInputException()
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
		}).orElse(super.getLocalizedMessage());
	}
}
