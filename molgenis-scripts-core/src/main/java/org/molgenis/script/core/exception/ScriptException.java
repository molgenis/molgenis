package org.molgenis.script.core.exception;

import org.molgenis.data.CodedRuntimeException;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public abstract class ScriptException extends CodedRuntimeException
{
	ScriptException(String errorCode)
	{
		super(errorCode);
	}

	ScriptException(String errorCode, Throwable cause)
	{
		super(errorCode, cause);
	}
}
