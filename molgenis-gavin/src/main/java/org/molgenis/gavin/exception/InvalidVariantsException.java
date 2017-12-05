package org.molgenis.gavin.exception;

import org.molgenis.data.CodedRuntimeException;

import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class InvalidVariantsException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "G03";

	public InvalidVariantsException()
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
