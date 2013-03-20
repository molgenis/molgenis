package org.molgenis.framework.db;

public class EntityNotFoundException extends DatabaseException
{
	private static final long serialVersionUID = 1L;

	public EntityNotFoundException(String message)
	{
		super(message);
	}

	public EntityNotFoundException(Exception exception)
	{
		super(exception);
	}

	public EntityNotFoundException(String message, Exception exception)
	{
		super(message, exception);
	}
}
