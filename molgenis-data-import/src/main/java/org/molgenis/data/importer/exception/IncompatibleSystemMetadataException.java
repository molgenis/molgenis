package org.molgenis.data.importer.exception;

import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class IncompatibleSystemMetadataException extends ImporterException
{
	private static final String ERROR_CODE = "I04";

	public IncompatibleSystemMetadataException()
	{
		super(ERROR_CODE);
	}

	@Override
	public String getMessage()
	{
		return String.format("Incompatible SystemEntityMetadata");
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService -> languageService.getString(ERROR_CODE))
								   .orElseGet(super::getLocalizedMessage);
	}
}
