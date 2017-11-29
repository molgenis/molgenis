package org.molgenis.script.core.exception;

@SuppressWarnings("squid:MaximumInheritanceDepth")
abstract class ScriptGenerationException extends ScriptException
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
