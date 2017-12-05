package org.molgenis.gavin.exception;

import org.molgenis.data.CodedRuntimeException;

import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class JobNotFoundException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "G04";

	public JobNotFoundException()
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
