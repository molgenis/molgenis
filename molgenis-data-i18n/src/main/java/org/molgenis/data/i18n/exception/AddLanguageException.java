package org.molgenis.data.i18n.exception;

import org.molgenis.data.CodedRuntimeException;

import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class AddLanguageException extends CodedRuntimeException
{
	private final static String ERROR_CODE = "L01";

	public AddLanguageException()
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
		}).orElseGet(super::getLocalizedMessage);
	}
}