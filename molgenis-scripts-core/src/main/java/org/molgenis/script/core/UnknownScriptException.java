package org.molgenis.script.core;

/**
 * @deprecated use class that extends from org.molgenis.i18n.CodedRuntimeException
 */
@Deprecated
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
