package org.molgenis.script.core.exception;

import java.text.MessageFormat;

import static java.lang.String.format;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

public class ScriptExecutionException extends ScriptException
{
	private static final String ERROR_CODE = "SC04";

	public ScriptExecutionException(String causeMessage)
	{
		super(ERROR_CODE, new RuntimeException(causeMessage));
	}

	public ScriptExecutionException(Throwable cause)
	{
		super(ERROR_CODE, cause);
	}

	@Override
	public String getMessage()
	{
		return format("cause:%s", getCause().getMessage());
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, getCause().getLocalizedMessage());
		}).orElse(super.getLocalizedMessage());
	}
}
