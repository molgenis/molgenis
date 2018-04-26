package org.molgenis.script.core;

/**
 * @deprecated use class that extends from org.molgenis.i18n.CodedRuntimeException
 */
@Deprecated
public class ScriptException extends RuntimeException
{
	private static final long serialVersionUID = -3077566548964401004L;

	public ScriptException()
	{
	}

	public ScriptException(String message)
	{
		super(message);
	}

	public ScriptException(Throwable cause)
	{
		super(cause);
	}

	public ScriptException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
