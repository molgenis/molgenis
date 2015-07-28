package org.molgenis.ui.form;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class MolgenisEntityFormSecurityException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public MolgenisEntityFormSecurityException()
	{
	}

	public MolgenisEntityFormSecurityException(String msg)
	{
		super(msg);
	}

	public MolgenisEntityFormSecurityException(Throwable t)
	{
		super(t);
	}

	public MolgenisEntityFormSecurityException(String msg, Throwable t)
	{
		super(msg, t);
	}

}
