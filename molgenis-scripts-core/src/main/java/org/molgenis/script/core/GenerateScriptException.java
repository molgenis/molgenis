package org.molgenis.script.core;

/**
 * @deprecated use class that extends from org.molgenis.i18n.CodedRuntimeException
 */
@Deprecated
public class GenerateScriptException extends ScriptException
{
	private static final long serialVersionUID = 6564516375098821636L;

	public GenerateScriptException()
	{
	}

	public GenerateScriptException(String message)
	{
		super(message);
	}

	public GenerateScriptException(Throwable cause)
	{
		super(cause);
	}

	public GenerateScriptException(String message, Throwable cause)
	{
		super(message, cause);
	}

}
