package org.molgenis.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.sql.SQLException;

import org.molgenis.data.MolgenisDataException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.transaction.TransactionSystemException;
import org.testng.annotations.Test;

public class SQLExceptionTranslatorTemplateTest
{
	@Test(expectedExceptions = MolgenisDataException.class)
	public void tryCatchSQLExceptionInsideUncategorizedSQLException()
	{
		SQLExceptionTranslatorTemplate.tryCatchSQLException(() -> {
			throw new UncategorizedSQLException("", "", new SQLException("", "",
					SQLExceptionTranslatorTemplate.MYSQL_ERROR_CODE_INCORRECT_STRING_VALUE));
		});
	}

	@Test(expectedExceptions = MolgenisDataException.class)
	public void tryCatchSQLExceptionInsideTransactionSystemException()
	{
		SQLExceptionTranslatorTemplate.tryCatchSQLException(() -> {
			throw new TransactionSystemException("", new SQLException("", "",
					SQLExceptionTranslatorTemplate.MYSQL_ERROR_CODE_INCORRECT_STRING_VALUE));
		});
	}

	@Test(expectedExceptions = UncategorizedSQLException.class)
	public void tryCatchUnknownSQLExceptionInsideUncategorizedSQLException()
	{
		SQLExceptionTranslatorTemplate.tryCatchSQLException(() -> {
			throw new UncategorizedSQLException("", "", new SQLException("", "", 99));
		});
	}

	@Test
	public void tryCatchSQLExceptionNoException()
	{
		SQLExceptionTranslatorTemplate.tryCatchSQLException(() -> {
		});
	}

	@Test(expectedExceptions = UncheckedIOException.class)
	public void tryCatchSQLExceptionOtherException()
	{
		SQLExceptionTranslatorTemplate.tryCatchSQLException(() -> {
			throw new UncheckedIOException(new IOException());
		});
	}
}
