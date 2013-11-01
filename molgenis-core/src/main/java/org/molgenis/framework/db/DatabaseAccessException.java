package org.molgenis.framework.db;

import org.molgenis.data.MolgenisDataException;

public class DatabaseAccessException extends MolgenisDataException
{
	private static final long serialVersionUID = 1L;

	public DatabaseAccessException(String message)
	{
		super(message);
	}

	public DatabaseAccessException(Exception exception)
	{
		super(exception);
	}

	public DatabaseAccessException(String message, Exception exception)
	{
		super(message, exception);
	}
}
