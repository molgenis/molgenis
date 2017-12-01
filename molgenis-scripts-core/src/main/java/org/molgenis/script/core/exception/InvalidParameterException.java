package org.molgenis.script.core.exception;

import java.text.MessageFormat;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

/**
 * Thrown when a script template can not be generated because of faulty parameter(s).
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class InvalidParameterException extends ScriptGenerationException
{
	private static final String ERROR_CODE = "SC02";
	private final String name;

	public InvalidParameterException(String name, Exception cause)
	{
		super(ERROR_CODE, cause);
		this.name = requireNonNull(name);
	}

	public String getName()
	{
		return name;
	}

	@Override
	public String getMessage()
	{
		return format("name:%s cause:%s", name, getCause().getMessage());
	}

	@Override
	public String getLocalizedMessage()
	{
		return getLanguageService().map(languageService ->
		{
			String format = languageService.getString(ERROR_CODE);
			return MessageFormat.format(format, name, getCause().getLocalizedMessage());
		}).orElse(super.getLocalizedMessage());
	}
}
