package org.molgenis.util;

import java.sql.SQLException;

/**
 * Wraps an {@link SQLException} with an unchecked exception.
 */
public class UncheckedSqlException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public UncheckedSqlException(SQLException e)
	{
		super(e);
	}

	@Override
	public SQLException getCause()
	{
		return (SQLException) super.getCause();
	}
}
