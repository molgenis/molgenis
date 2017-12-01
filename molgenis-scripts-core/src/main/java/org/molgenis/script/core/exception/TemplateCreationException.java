package org.molgenis.script.core.exception;

import java.io.IOException;
import java.text.MessageFormat;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.i18n.LanguageServiceHolder.getLanguageService;

/**
 * Wraps exceptions that can occur during the creation of a Template for a script.
 */
@SuppressWarnings("MaximumInheritanceDepth")
public class TemplateCreationException extends ScriptGenerationException
{
	private static final String ERROR_CODE = "SC04";
	private final String name;

	public TemplateCreationException(String name, IOException cause)
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
			return MessageFormat.format(format, getCause().getLocalizedMessage());
		}).orElse(super.getLocalizedMessage());
	}
}
