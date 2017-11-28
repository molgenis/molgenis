package org.molgenis.script.core.exception;

import java.text.MessageFormat;

import static java.lang.String.format;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class MissingParameterException extends ScriptGenerationException
{
	private static final String ERROR_CODE = "SC03";

	private final String parameterName;

	public MissingParameterException(String parameterName)
	{
		super(ERROR_CODE);
		this.parameterName = parameterName;
	}

	public String getParameterName()
	{
		return parameterName;
	}

	@Override
	public String getMessage()
	{
		return format("param:%s", parameterName);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, parameterName);
		}).orElse(super.getLocalizedMessage());
	}
}
