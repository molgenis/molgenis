package org.molgenis.script.core.exception;

public class ScriptGenerationException extends ScriptException
{
	public ScriptGenerationException(String errorCode)
	{
		super(errorCode);
	}

	public ScriptGenerationException(String errorCode, Throwable cause)
	{
		super(errorCode, cause);
	}
}
