package org.molgenis.data.rest.exception;

import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

/**
 * thrown if an update was attempeted on an entity with a file attribute, but without sending the value(the file) for this attribute
 */
public class FileAttributeUpdateWithoutFileException extends RestApiException
{
	private final static String ERROR_CODE = "R02";

	public FileAttributeUpdateWithoutFileException()
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
			return languageService.getString(ERROR_CODE);
		}).orElse(super.getLocalizedMessage());
	}
}
