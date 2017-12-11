package org.molgenis.script.core.exception;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * Wraps exceptions that can occur during the execution of a script.
 */
// S2166 'Classes named like "Exception" should extend "Exception" or a subclass' often gives false positives at dev time
@SuppressWarnings({ "squid:MaximumInheritanceDepth", "squid:S2166" })
public class ScriptExecutionException extends ScriptException
{
	private static final String ERROR_CODE = "SC04";

	private final String causeMessage;

	public ScriptExecutionException(String causeMessage)
	{
		super(ERROR_CODE);
		this.causeMessage = requireNonNull(causeMessage);
	}

	public ScriptExecutionException(Throwable cause)
	{
		super(ERROR_CODE, cause);
		this.causeMessage = cause.getLocalizedMessage();
	}

	@Override
	public String getMessage()
	{
		return format("cause:%s", causeMessage);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { causeMessage };
	}
}
