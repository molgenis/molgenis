package org.molgenis.data;

import org.springframework.dao.DataAccessException;

//Deprecated Exception that is used in the postgresException translation
@Deprecated
public class MolgenisDataAccessException extends DataAccessException
{
	public MolgenisDataAccessException()
	{
		this("");
	}

	public MolgenisDataAccessException(String msg)
	{
		super(msg);
	}

	public MolgenisDataAccessException(Throwable t)
	{
		this("", t);
	}

	public MolgenisDataAccessException(String msg, Throwable t)
	{
		super(msg, t);
	}
}
