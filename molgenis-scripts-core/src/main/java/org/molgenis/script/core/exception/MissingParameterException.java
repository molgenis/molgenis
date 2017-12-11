package org.molgenis.script.core.exception;

import static java.lang.String.format;

/**
 * Thrown when there's a parameter missing when generating a script.
 */
// S2166 'Classes named like "Exception" should extend "Exception" or a subclass' often gives false positives at dev time
@SuppressWarnings({ "squid:MaximumInheritanceDepth", "squid:S2166" })
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
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { parameterName };
	}
}
