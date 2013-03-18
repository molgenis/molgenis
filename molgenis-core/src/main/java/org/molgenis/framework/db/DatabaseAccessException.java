package org.molgenis.framework.db;

public class DatabaseAccessException extends DatabaseException
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
