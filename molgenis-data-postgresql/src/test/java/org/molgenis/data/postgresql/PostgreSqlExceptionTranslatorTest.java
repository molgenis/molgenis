package org.molgenis.data.postgresql;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import javax.sql.DataSource;

import org.molgenis.data.validation.MolgenisValidationException;
import org.postgresql.util.PSQLException;
import org.postgresql.util.ServerErrorMessage;
import org.testng.annotations.Test;

public class PostgreSqlExceptionTranslatorTest
{
	@Test(expectedExceptions = NullPointerException.class)
	public void PostgreSqlExceptionTranslator()
	{
		new PostgreSqlExceptionTranslator(null);
	}

	@Test
	public void translateNotNullViolation()
	{
		DataSource dataSource = mock(DataSource.class);
		PostgreSqlExceptionTranslator postgreSqlExceptionTranslator = new PostgreSqlExceptionTranslator(dataSource);
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getSQLState()).thenReturn("23502");
		when(serverErrorMessage.getTable()).thenReturn("mytable");
		when(serverErrorMessage.getMessage())
				.thenReturn("null value in column \"mycolumn\" violates not-null constraint");
		MolgenisValidationException e = postgreSqlExceptionTranslator
				.translateNotNullViolation(new PSQLException(serverErrorMessage));
		assertEquals(e.getMessage(), "The attribute 'mycolumn' of entity 'mytable' can not be null.");
	}

	@Test(expectedExceptions = RuntimeException.class)
	public void translateNotNullViolationBadMessage()
	{
		DataSource dataSource = mock(DataSource.class);
		PostgreSqlExceptionTranslator postgreSqlExceptionTranslator = new PostgreSqlExceptionTranslator(dataSource);
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getSQLState()).thenReturn("23502");
		when(serverErrorMessage.getTable()).thenReturn("mytable");
		when(serverErrorMessage.getMessage()).thenReturn("xxxyyyzzzz");
		postgreSqlExceptionTranslator.translateNotNullViolation(new PSQLException(serverErrorMessage));
	}

	@Test
	public void translateForeignKeyViolation()
	{
		DataSource dataSource = mock(DataSource.class);
		PostgreSqlExceptionTranslator postgreSqlExceptionTranslator = new PostgreSqlExceptionTranslator(dataSource);
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getSQLState()).thenReturn("23503");
		when(serverErrorMessage.getTable()).thenReturn("mytable");
		when(serverErrorMessage.getDetail()).thenReturn("... (myvalue) ... (mycolumn) ...");
		MolgenisValidationException e = postgreSqlExceptionTranslator
				.translateForeignKeyViolation(new PSQLException(serverErrorMessage));
		assertEquals(e.getMessage(), "Unknown xref value 'myvalue' for attribute 'mycolumn' of entity 'mytable'.");
	}

	@Test(expectedExceptions = RuntimeException.class)
	public void translateForeignKeyViolationBadMessage()
	{
		DataSource dataSource = mock(DataSource.class);
		PostgreSqlExceptionTranslator postgreSqlExceptionTranslator = new PostgreSqlExceptionTranslator(dataSource);
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getSQLState()).thenReturn("23503");
		when(serverErrorMessage.getTable()).thenReturn("mytable");
		when(serverErrorMessage.getDetail()).thenReturn("xxxyyyyzzzz");
		postgreSqlExceptionTranslator.translateForeignKeyViolation(new PSQLException(serverErrorMessage));
	}

	@Test
	public void translateUniqueKeyViolation()
	{
		DataSource dataSource = mock(DataSource.class);
		PostgreSqlExceptionTranslator postgreSqlExceptionTranslator = new PostgreSqlExceptionTranslator(dataSource);
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getSQLState()).thenReturn("23505");
		when(serverErrorMessage.getTable()).thenReturn("mytable");
		when(serverErrorMessage.getDetail()).thenReturn("Key (mycolumn)=(myvalue) already exists.");
		MolgenisValidationException e = postgreSqlExceptionTranslator
				.translateUniqueKeyViolation(new PSQLException(serverErrorMessage));
		assertEquals(e.getMessage(),
				"Duplicate value 'myvalue' for unique attribute 'mycolumn' from entity 'mytable'.");
	}

	@Test(expectedExceptions = RuntimeException.class)
	public void translateUniqueKeyViolationBadMessage()
	{
		DataSource dataSource = mock(DataSource.class);
		PostgreSqlExceptionTranslator postgreSqlExceptionTranslator = new PostgreSqlExceptionTranslator(dataSource);
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getSQLState()).thenReturn("23505");
		when(serverErrorMessage.getTable()).thenReturn("mytable");
		when(serverErrorMessage.getDetail()).thenReturn("xxxyyyyzzz");
		postgreSqlExceptionTranslator.translateUniqueKeyViolation(new PSQLException(serverErrorMessage));
	}
}
