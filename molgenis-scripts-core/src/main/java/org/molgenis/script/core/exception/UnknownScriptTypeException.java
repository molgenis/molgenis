package org.molgenis.script.core.exception;

import java.text.MessageFormat;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class UnknownScriptTypeException extends ScriptException
{
	private static final String ERROR_CODE = "SC01";

	private final String type;

	public UnknownScriptTypeException(String type)
	{
		super(ERROR_CODE);
		this.type = requireNonNull(type);
	}

	public String getType()
	{
		return type;
	}

	@Override
	public String getMessage()
	{
		return format("type:%s", type);
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, type);
		}).orElse(super.getLocalizedMessage());
	}
}
