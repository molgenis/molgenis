package org.molgenis.ui.style;

import org.molgenis.data.CodedRuntimeException;

import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class CannotDeleteAllThemesException extends CodedRuntimeException
{
	private final static String ERROR_CODE = "C03";

	public CannotDeleteAllThemesException()
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
