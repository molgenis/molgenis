package org.molgenis.data.rest.exception;

import org.molgenis.data.CodedRuntimeException;

import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class FileAttributeUpdateWithoutFileException extends CodedRuntimeException
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
