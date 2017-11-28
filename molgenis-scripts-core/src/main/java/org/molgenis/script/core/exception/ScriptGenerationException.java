package org.molgenis.script.core.exception;

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
