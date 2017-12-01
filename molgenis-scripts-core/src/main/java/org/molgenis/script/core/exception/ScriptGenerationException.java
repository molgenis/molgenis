package org.molgenis.script.core.exception;

/**
 * The generic class for exceptions that should be thrown when exceptions occur during the generation of a script.
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public abstract class ScriptGenerationException extends ScriptException
{
	ScriptGenerationException(String errorCode)
	{
		super(errorCode);
	}

	ScriptGenerationException(String errorCode, Throwable cause)
	{
		super(errorCode, cause);
	}
}
