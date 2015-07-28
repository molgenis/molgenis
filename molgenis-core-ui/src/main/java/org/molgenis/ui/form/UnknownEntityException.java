package org.molgenis.ui.form;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class UnknownEntityException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public UnknownEntityException()
	{
	}

	public UnknownEntityException(String msg)
	{
		super(msg);
	}

	public UnknownEntityException(Throwable t)
	{
		super(t);
	}

	public UnknownEntityException(String msg, Throwable t)
	{
		super(msg, t);
	}

}
