package org.molgenis.security.user;

import org.molgenis.data.MolgenisRuntimeException;

public class MolgenisUserException extends MolgenisRuntimeException
{

	private static final long serialVersionUID = -8400330400566838323L;
	String message = "";

	public MolgenisUserException(String message)
	{
		super(message);
		this.message = message;
	}

	public MolgenisUserException(Exception exception)
	{
		super(exception);
		this.message = exception.getMessage();
	}

	@Override
	public String getMessage()
	{
		return this.message;
	}
}
