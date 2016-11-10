package org.molgenis.data.postgresql;

import org.molgenis.data.validation.MolgenisValidationException;
import org.postgresql.util.PSQLException;
import org.postgresql.util.ServerErrorMessage;
import org.testng.annotations.Test;

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
	public void translateDependentObjectsStillExistOneDependentTableSingleDependency()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getSQLState()).thenReturn("2BP01");
		when(serverErrorMessage.getDetail()).thenReturn(
				"constraint my_foreign_key_constraint on table \"myTable\" depends on table \"myDependentTable\"");
		//noinspection ThrowableResultOfMethodCallIgnored
		MolgenisValidationException e = PostgreSqlExceptionTranslator
				.translateDependentObjectsStillExist(new PSQLException(serverErrorMessage));
		assertEquals(e.getMessage(), "Cannot delete entity 'myTable' because entity 'myDependentTable' depends on it.");
	}

	@Test
	public void translateDependentObjectsStillExistOneDependentTableMultipleDependencies()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getSQLState()).thenReturn("2BP01");
		when(serverErrorMessage.getDetail()).thenReturn(
				"constraint my_foreign_key_constraint on table \"myTable\" depends on table \"myDependentTable\"\nconstraint myOther_foreign_key_constraint on table \"myTable\" depends on table \"myDependentTable\"");
		//noinspection ThrowableResultOfMethodCallIgnored
		MolgenisValidationException e = PostgreSqlExceptionTranslator
				.translateDependentObjectsStillExist(new PSQLException(serverErrorMessage));
		assertEquals(e.getMessage(), "Cannot delete entity 'myTable' because entity 'myDependentTable' depends on it.");
	}

	@Test
	public void translateDependentObjectsStillExistMultipleDependentTables()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getSQLState()).thenReturn("2BP01");
		when(serverErrorMessage.getDetail()).thenReturn(
				"constraint my_foreign_key_constraint on table \"myTable\" depends on table \"myDependentTable\"\nconstraint myOther_foreign_key_constraint on table \"myTable\" depends on table \"myOtherDependentTable\"");
		//noinspection ThrowableResultOfMethodCallIgnored
		MolgenisValidationException e = PostgreSqlExceptionTranslator
				.translateDependentObjectsStillExist(new PSQLException(serverErrorMessage));
		assertEquals(e.getMessage(),
				"Cannot delete entity 'myTable' because entities 'myDependentTable, myOtherDependentTable' depend on it.");
	}

	@Test
	public void translateDependentObjectsStillExistNoDoubleQuotes()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getSQLState()).thenReturn("2BP01");
		when(serverErrorMessage.getDetail())
				.thenReturn("constraint my_foreign_key_constraint on table xxx depends on table yyy");
		//noinspection ThrowableResultOfMethodCallIgnored
		MolgenisValidationException e = PostgreSqlExceptionTranslator
				.translateDependentObjectsStillExist(new PSQLException(serverErrorMessage));
		assertEquals(e.getMessage(), "Cannot delete entity 'xxx' because entity 'yyy' depends on it.");
	}

	@Test
	public void translateNotNullViolation()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getSQLState()).thenReturn("23502");
		when(serverErrorMessage.getTable()).thenReturn("myTable");
		when(serverErrorMessage.getMessage())
				.thenReturn("null value in column \"myColumn\" violates not-null constraint");
		//noinspection ThrowableResultOfMethodCallIgnored
		MolgenisValidationException e = PostgreSqlExceptionTranslator
				.translateNotNullViolation(new PSQLException(serverErrorMessage));
		assertEquals(e.getMessage(), "The attribute 'myColumn' of entity 'myTable' can not be null.");
	}

	@Test(expectedExceptions = RuntimeException.class)
	public void translateNotNullViolationBadMessage()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getSQLState()).thenReturn("23502");
		when(serverErrorMessage.getTable()).thenReturn("mytable");
		when(serverErrorMessage.getMessage()).thenReturn("xxxyyyzzzz");
		//noinspection ThrowableResultOfMethodCallIgnored
		PostgreSqlExceptionTranslator.translateNotNullViolation(new PSQLException(serverErrorMessage));
	}

	@Test
	public void translateNotNullViolationNoDoubleQuotes()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getSQLState()).thenReturn("23502");
		when(serverErrorMessage.getTable()).thenReturn("xxx");
		when(serverErrorMessage.getMessage()).thenReturn("null value in column yyy violates not-null constraint");
		//noinspection ThrowableResultOfMethodCallIgnored
		MolgenisValidationException e = PostgreSqlExceptionTranslator
				.translateNotNullViolation(new PSQLException(serverErrorMessage));
		assertEquals(e.getMessage(), "The attribute 'yyy' of entity 'xxx' can not be null.");
	}

	@Test
	public void translateForeignKeyViolation()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getSQLState()).thenReturn("23503");
		when(serverErrorMessage.getTable()).thenReturn("mytable");
		when(serverErrorMessage.getDetail()).thenReturn("... (mycolumn) ... (myvalue) ...");
		//noinspection ThrowableResultOfMethodCallIgnored
		MolgenisValidationException e = PostgreSqlExceptionTranslator
				.translateForeignKeyViolation(new PSQLException(serverErrorMessage));
		assertEquals(e.getMessage(), "Unknown xref value 'myvalue' for attribute 'mycolumn' of entity 'mytable'.");
	}

	@Test
	public void translateForeignKeyViolationNotPresent()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getSQLState()).thenReturn("23503");
		when(serverErrorMessage.getTable()).thenReturn("mytable");
		when(serverErrorMessage.getDetail()).thenReturn("Key (mycolumn)=(myvalue) is not present in table \"mytable\"");
		//noinspection ThrowableResultOfMethodCallIgnored
		MolgenisValidationException e = PostgreSqlExceptionTranslator
				.translateForeignKeyViolation(new PSQLException(serverErrorMessage));
		assertEquals(e.getMessage(), "Unknown xref value 'myvalue' for attribute 'mycolumn' of entity 'mytable'.");
	}

	@Test
	public void translateForeignKeyViolationStillReferenced()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getSQLState()).thenReturn("23503");
		when(serverErrorMessage.getTable()).thenReturn("mytable");
		when(serverErrorMessage.getDetail())
				.thenReturn("Key (mycolumn)=(myvalue) is still referenced from table \"mytable\"");
		//noinspection ThrowableResultOfMethodCallIgnored
		MolgenisValidationException e = PostgreSqlExceptionTranslator
				.translateForeignKeyViolation(new PSQLException(serverErrorMessage));
		assertEquals(e.getMessage(), "Value 'myvalue' for attribute 'mycolumn' is referenced by entity 'mytable'.");
	}

	@Test(expectedExceptions = RuntimeException.class)
	public void translateForeignKeyViolationBadMessage()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getSQLState()).thenReturn("23503");
		when(serverErrorMessage.getTable()).thenReturn("mytable");
		when(serverErrorMessage.getDetail()).thenReturn("xxxyyyyzzzz");
		//noinspection ThrowableResultOfMethodCallIgnored
		PostgreSqlExceptionTranslator.translateForeignKeyViolation(new PSQLException(serverErrorMessage));
	}

	@Test
	public void translateUniqueKeyViolation()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getSQLState()).thenReturn("23505");
		when(serverErrorMessage.getTable()).thenReturn("mytable");
		when(serverErrorMessage.getDetail()).thenReturn("Key (mycolumn)=(myvalue) already exists.");
		//noinspection ThrowableResultOfMethodCallIgnored
		MolgenisValidationException e = PostgreSqlExceptionTranslator
				.translateUniqueKeyViolation(new PSQLException(serverErrorMessage));
		assertEquals(e.getMessage(),
				"Duplicate value 'myvalue' for unique attribute 'mycolumn' from entity 'mytable'.");
	}

	@Test(expectedExceptions = RuntimeException.class)
	public void translateUniqueKeyViolationBadMessage()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getSQLState()).thenReturn("23505");
		when(serverErrorMessage.getTable()).thenReturn("mytable");
		when(serverErrorMessage.getDetail()).thenReturn("xxxyyyyzzz");
		//noinspection ThrowableResultOfMethodCallIgnored
		PostgreSqlExceptionTranslator.translateUniqueKeyViolation(new PSQLException(serverErrorMessage));
	}

	@Test
	public void translateInvalidIntegerExceptionInteger()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getMessage()).thenReturn("invalid input syntax for integer: \"str1\"");
		//noinspection ThrowableResultOfMethodCallIgnored
		MolgenisValidationException e = PostgreSqlExceptionTranslator
				.translateInvalidIntegerException(new PSQLException(serverErrorMessage));
		assertEquals(e.getMessage(), "Value [str1] of this entity attribute is not of type [INT or LONG].");
	}

	@Test
	public void translateInvalidIntegerExceptionBoolean()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getMessage()).thenReturn("invalid input syntax for type boolean: \"str1\"");
		//noinspection ThrowableResultOfMethodCallIgnored
		MolgenisValidationException e = PostgreSqlExceptionTranslator
				.translateInvalidIntegerException(new PSQLException(serverErrorMessage));
		assertEquals(e.getMessage(), "Value [str1] of this entity attribute is not of type [BOOL].");
	}

	@Test
	public void translateInvalidIntegerExceptionDouble()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getMessage()).thenReturn("invalid input syntax for type double precision: \"str1\"");
		//noinspection ThrowableResultOfMethodCallIgnored
		MolgenisValidationException e = PostgreSqlExceptionTranslator
				.translateInvalidIntegerException(new PSQLException(serverErrorMessage));
		assertEquals(e.getMessage(), "Value [str1] of this entity attribute is not of type [DECIMAL].");
	}

	@Test
	public void translateInvalidIntegerExceptionDate()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getMessage()).thenReturn("invalid input syntax for type date: \"str1\"");
		//noinspection ThrowableResultOfMethodCallIgnored
		MolgenisValidationException e = PostgreSqlExceptionTranslator
				.translateInvalidIntegerException(new PSQLException(serverErrorMessage));
		assertEquals(e.getMessage(), "Value [str1] of this entity attribute is not of type [DATE].");
	}

	@Test
	public void translateInvalidIntegerExceptionDateTime()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getMessage()).thenReturn("invalid input syntax for type timestamp: \"str1\"");
		//noinspection ThrowableResultOfMethodCallIgnored
		MolgenisValidationException e = PostgreSqlExceptionTranslator
				.translateInvalidIntegerException(new PSQLException(serverErrorMessage));
		assertEquals(e.getMessage(), "Value [str1] of this entity attribute is not of type [DATE_TIME].");
	}

	@Test
	public void translateCheckConstraintViolation()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getTable()).thenReturn("entity");
		when(serverErrorMessage.getConstraint()).thenReturn("entity_column_chk");
		//noinspection ThrowableResultOfMethodCallIgnored
		MolgenisValidationException e = PostgreSqlExceptionTranslator
				.translateCheckConstraintViolation(new PSQLException(serverErrorMessage));
		assertEquals(e.getMessage(), "Unknown enum value for attribute 'column' of entity 'entity'.");
	}

	@Test
	public void translateUndefinedColumnException()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getSQLState()).thenReturn("42703");
		when(serverErrorMessage.getMessage()).thenReturn("Undefined column: 7 ERROR: column \"test\" does not exist");
		//noinspection ThrowableResultOfMethodCallIgnored
		MolgenisValidationException e = PostgreSqlExceptionTranslator
				.translateUndefinedColumnException(new PSQLException(serverErrorMessage));
		assertEquals(e.getMessage(), "Undefined column: 7 ERROR: column \"test\" does not exist");
	}
}
