package org.molgenis.script.core.exception;

import java.io.IOException;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * Wraps exceptions that can occur during the creation of a Template for a script.
 */
// S2166 'Classes named like "Exception" should extend "Exception" or a subclass' often gives false positives at dev time
@SuppressWarnings({ "squid:MaximumInheritanceDepth", "squid:S2166" })
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
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { getCause().getLocalizedMessage() };
	}
}
