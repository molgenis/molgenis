package org.molgenis.script.core.exception;

import org.molgenis.data.CodedRuntimeException;

public class ScriptException extends CodedRuntimeException
{
	public ScriptException(String errorCode)
	{
		super(errorCode);
	}

	public ScriptException(String errorCode, Throwable cause)
	{
		super(errorCode, cause);
	}
}
