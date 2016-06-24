package org.molgenis.util;

import java.sql.SQLException;

import org.molgenis.data.MolgenisDataException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.transaction.TransactionSystemException;

/**
 * Translate SQLException to a MolgenisDataException
 */
public class SQLExceptionTranslatorTemplate
{
	public static final int MYSQL_ERROR_CODE_INCORRECT_STRING_VALUE = 1366;

	public static void tryCatchSQLException(Runnable func) throws MolgenisDataException
	{
		try
		{
			func.run();
		}
		catch (UncategorizedSQLException e)
		{
			SQLException sqlEx = e.getSQLException();
			if (sqlEx != null)
			{
				translateException(sqlEx);
			}

			throw e;
		}
		catch (TransactionSystemException e)
		{
			Throwable cause = e.getMostSpecificCause();
			if ((cause != null) && (cause instanceof SQLException))
			{
				translateException((SQLException) cause);
			}

			throw e;
		}
	}

	private static void translateException(SQLException e)
	{
		if (e.getErrorCode() == MYSQL_ERROR_CODE_INCORRECT_STRING_VALUE)
		{
			throw new MolgenisDataException("Your input contains one or more invalid characters.");
		}
	}
}
