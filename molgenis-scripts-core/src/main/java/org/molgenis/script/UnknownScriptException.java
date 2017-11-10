package org.molgenis.script;

@Deprecated // FIXME extend from LocalizedRuntimeException
public class UnknownScriptException extends ScriptException
{
	private static final long serialVersionUID = 4808852834196162476L;

	public UnknownScriptException()
	{
	}

	public UnknownScriptException(String message)
	{
		super(message);
	}

	public UnknownScriptException(Throwable cause)
	{
		super(cause);
	}

	public UnknownScriptException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
