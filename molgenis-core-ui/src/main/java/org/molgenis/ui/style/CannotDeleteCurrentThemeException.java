package org.molgenis.ui.style;

import org.molgenis.data.CodedRuntimeException;

import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class CannotDeleteCurrentThemeException extends CodedRuntimeException
{
	private final static String ERROR_CODE = "C04";

	public CannotDeleteCurrentThemeException()
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
