package org.molgenis.framework.db;

/**
 * A small class defining a database exception.
 * 
 * @author ?
 * 
 */
public class DatabaseException extends Exception
{
	String message = "";
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DatabaseException(String message)
	{
		super(message);
		this.message = message;
	}

	public DatabaseException(Exception exception)
	{
		super(exception);
		message = exception.getMessage();
	}

	public DatabaseException(String message, Exception exception)
	{
		super(exception);
		this.message = message;
	}

	@Override
	public String getMessage()
	{
		return message;
	}
}
