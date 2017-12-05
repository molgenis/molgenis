package org.molgenis.script.core.exception;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * Thrown when a script template can not be generated because of faulty parameter(s).
 */
// S2166 'Classes named like "Exception" should extend "Exception" or a subclass' often gives false positives at dev time
@SuppressWarnings({ "squid:MaximumInheritanceDepth", "squid:S2166" })
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
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { name, getCause().getLocalizedMessage() };
	}
}
