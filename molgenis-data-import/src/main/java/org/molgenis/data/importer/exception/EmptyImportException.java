package org.molgenis.data.importer.exception;

import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class EmptyImportException extends ImporterException
{
	private static final String ERROR_CODE = "I06";

	public EmptyImportException()
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
		return getLanguageService().map(languageService -> languageService.getString(ERROR_CODE))
								   .orElseGet(super::getLocalizedMessage);
	}
}
