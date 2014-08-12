package org.molgenis.script;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
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
