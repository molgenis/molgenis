package org.molgenis.script.core.exception;

import org.molgenis.data.CodedRuntimeException;

// S2166 'Classes named like "Exception" should extend "Exception" or a subclass' often gives false positives at dev time
@SuppressWarnings({ "squid:MaximumInheritanceDepth", "squid:S2166" })
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
