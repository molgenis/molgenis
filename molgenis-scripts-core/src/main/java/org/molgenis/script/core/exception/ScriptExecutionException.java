package org.molgenis.script.core.exception;

import java.util.Objects;

/**
 * Wraps exception or error message that can occurs during the execution of a script.
 */
// S2166 'Classes named like "Exception" should extend "Exception" or a subclass' often gives false positives at dev time
@SuppressWarnings({ "squid:MaximumInheritanceDepth", "squid:S2166" })
public class ScriptExecutionException extends ScriptException
{
	private static final String ERROR_CODE = "SC04";

	private final String errorMessage;

	public ScriptExecutionException(String errorMessage)
	{
		this(errorMessage, null);
	}

	public ScriptExecutionException(Throwable cause)
	{
		this("", cause);
	}

	private ScriptExecutionException(String errorMessage, Throwable cause)
	{
		super(ERROR_CODE, cause);
		this.errorMessage = Objects.requireNonNull(errorMessage);
	}

	@Override
	public String getMessage()
	{
		return String.format("errorMessage:%s", errorMessage);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { errorMessage };
	}
}
