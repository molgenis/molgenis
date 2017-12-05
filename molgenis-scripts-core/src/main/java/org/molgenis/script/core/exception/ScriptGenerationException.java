package org.molgenis.script.core.exception;

/**
 * The generic class for exceptions that should be thrown when exceptions occur during the generation of a script.
 */
// S2166 'Classes named like "Exception" should extend "Exception" or a subclass' often gives false positives at dev time
@SuppressWarnings({ "squid:MaximumInheritanceDepth", "squid:S2166" })
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
