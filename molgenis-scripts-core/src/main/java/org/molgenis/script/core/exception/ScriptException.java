package org.molgenis.script.core.exception;

import org.molgenis.data.CodedRuntimeException;

@SuppressWarnings("squid:MaximumInheritanceDepth")
abstract class ScriptException extends CodedRuntimeException
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
