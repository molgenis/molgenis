package org.molgenis.data.postgresql;

import org.molgenis.data.validation.MolgenisValidationException;
import org.postgresql.util.PSQLException;
import org.postgresql.util.ServerErrorMessage;
import org.testng.annotations.Test;

import javax.sql.DataSource;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

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
		//noinspection ThrowableResultOfMethodCallIgnored
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
		//noinspection ThrowableResultOfMethodCallIgnored
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
		when(serverErrorMessage.getDetail()).thenReturn("... (mycolumn) ... (myvalue) ...");
		//noinspection ThrowableResultOfMethodCallIgnored
		MolgenisValidationException e = postgreSqlExceptionTranslator
				.translateForeignKeyViolation(new PSQLException(serverErrorMessage));
		assertEquals(e.getMessage(), "Unknown xref value 'myvalue' for attribute 'mycolumn' of entity 'mytable'.");
	}

	@Test
	public void translateForeignKeyViolationNotPresent()
	{
		DataSource dataSource = mock(DataSource.class);
		PostgreSqlExceptionTranslator postgreSqlExceptionTranslator = new PostgreSqlExceptionTranslator(dataSource);
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getSQLState()).thenReturn("23503");
		when(serverErrorMessage.getTable()).thenReturn("mytable");
		when(serverErrorMessage.getDetail()).thenReturn("Key (mycolumn)=(myvalue) is not present in table \"mytable\"");
		//noinspection ThrowableResultOfMethodCallIgnored
		MolgenisValidationException e = postgreSqlExceptionTranslator
				.translateForeignKeyViolation(new PSQLException(serverErrorMessage));
		assertEquals(e.getMessage(), "Unknown xref value 'myvalue' for attribute 'mycolumn' of entity 'mytable'.");
	}

	@Test
	public void translateForeignKeyViolationStillReferenced()
	{
		DataSource dataSource = mock(DataSource.class);
		PostgreSqlExceptionTranslator postgreSqlExceptionTranslator = new PostgreSqlExceptionTranslator(dataSource);
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getSQLState()).thenReturn("23503");
		when(serverErrorMessage.getTable()).thenReturn("mytable");
		when(serverErrorMessage.getDetail())
				.thenReturn("Key (mycolumn)=(myvalue) is still referenced from table \"mytable\"");
		//noinspection ThrowableResultOfMethodCallIgnored
		MolgenisValidationException e = postgreSqlExceptionTranslator
				.translateForeignKeyViolation(new PSQLException(serverErrorMessage));
		assertEquals(e.getMessage(), "Value 'myvalue' for attribute 'mycolumn' is referenced by entity 'mytable'.");
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
		//noinspection ThrowableResultOfMethodCallIgnored
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
		//noinspection ThrowableResultOfMethodCallIgnored
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
		//noinspection ThrowableResultOfMethodCallIgnored
		postgreSqlExceptionTranslator.translateUniqueKeyViolation(new PSQLException(serverErrorMessage));
	}

	@Test
	public void translateInvalidIntegerExceptionInteger()
	{
		DataSource dataSource = mock(DataSource.class);
		PostgreSqlExceptionTranslator postgreSqlExceptionTranslator = new PostgreSqlExceptionTranslator(dataSource);
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getMessage()).thenReturn("invalid input syntax for integer: \"str1\"");
		//noinspection ThrowableResultOfMethodCallIgnored
		MolgenisValidationException e = postgreSqlExceptionTranslator
				.translateInvalidIntegerException(new PSQLException(serverErrorMessage));
		assertEquals(e.getMessage(), "Value [str1] of this entity attribute is not of type [INT or LONG].");
	}

	@Test
	public void translateInvalidIntegerExceptionBoolean()
	{
		DataSource dataSource = mock(DataSource.class);
		PostgreSqlExceptionTranslator postgreSqlExceptionTranslator = new PostgreSqlExceptionTranslator(dataSource);
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getMessage()).thenReturn("invalid input syntax for type boolean: \"str1\"");
		//noinspection ThrowableResultOfMethodCallIgnored
		MolgenisValidationException e = postgreSqlExceptionTranslator
				.translateInvalidIntegerException(new PSQLException(serverErrorMessage));
		assertEquals(e.getMessage(), "Value [str1] of this entity attribute is not of type [BOOL].");
	}

	@Test
	public void translateInvalidIntegerExceptionDouble()
	{
		DataSource dataSource = mock(DataSource.class);
		PostgreSqlExceptionTranslator postgreSqlExceptionTranslator = new PostgreSqlExceptionTranslator(dataSource);
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getMessage()).thenReturn("invalid input syntax for type double precision: \"str1\"");
		//noinspection ThrowableResultOfMethodCallIgnored
		MolgenisValidationException e = postgreSqlExceptionTranslator
				.translateInvalidIntegerException(new PSQLException(serverErrorMessage));
		assertEquals(e.getMessage(), "Value [str1] of this entity attribute is not of type [DECIMAL].");
	}

	@Test
	public void translateInvalidIntegerExceptionDate()
	{
		DataSource dataSource = mock(DataSource.class);
		PostgreSqlExceptionTranslator postgreSqlExceptionTranslator = new PostgreSqlExceptionTranslator(dataSource);
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getMessage()).thenReturn("invalid input syntax for type date: \"str1\"");
		//noinspection ThrowableResultOfMethodCallIgnored
		MolgenisValidationException e = postgreSqlExceptionTranslator
				.translateInvalidIntegerException(new PSQLException(serverErrorMessage));
		assertEquals(e.getMessage(), "Value [str1] of this entity attribute is not of type [DATE].");
	}

	@Test
	public void translateInvalidIntegerExceptionDateTime()
	{
		DataSource dataSource = mock(DataSource.class);
		PostgreSqlExceptionTranslator postgreSqlExceptionTranslator = new PostgreSqlExceptionTranslator(dataSource);
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getMessage()).thenReturn("invalid input syntax for type timestamp: \"str1\"");
		//noinspection ThrowableResultOfMethodCallIgnored
		MolgenisValidationException e = postgreSqlExceptionTranslator
				.translateInvalidIntegerException(new PSQLException(serverErrorMessage));
		assertEquals(e.getMessage(), "Value [str1] of this entity attribute is not of type [DATE_TIME].");
	}
}
