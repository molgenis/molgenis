package org.molgenis.data.postgresql;

import com.google.common.collect.ImmutableMap;
import org.molgenis.data.postgresql.identifier.AttributeDescription;
import org.molgenis.data.postgresql.identifier.EntityTypeDescription;
import org.molgenis.data.postgresql.identifier.EntityTypeRegistry;
import org.molgenis.data.validation.MolgenisValidationException;
import org.postgresql.util.PSQLException;
import org.postgresql.util.ServerErrorMessage;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.sql.DataSource;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class PostgreSqlExceptionTranslatorTest
{

	private PostgreSqlExceptionTranslator postgreSqlExceptionTranslator;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		DataSource dataSource = mock(DataSource.class);
		EntityTypeRegistry entityTypeRegistry = mock(EntityTypeRegistry.class);
		EntityTypeDescription entityTypeDescription = EntityTypeDescription.create("myEntity",
				ImmutableMap.<String, AttributeDescription>builder().put("myColumn",
						AttributeDescription.create("myAttr")).build());
		EntityTypeDescription refEntityTypeDescription = EntityTypeDescription.create("myRefEntity",
				ImmutableMap.<String, AttributeDescription>builder().put("myColumn",
						AttributeDescription.create("myAttr")).build());
		EntityTypeDescription otherRefEntityTypeDescription = EntityTypeDescription.create("myOtherRefEntity",
				ImmutableMap.<String, AttributeDescription>builder().put("myColumn",
						AttributeDescription.create("myAttr")).build());
		when(entityTypeRegistry.getEntityTypeDescription("myTable")).thenReturn(entityTypeDescription);
		when(entityTypeRegistry.getEntityTypeDescription("myDependentTable")).thenReturn(refEntityTypeDescription);
		when(entityTypeRegistry.getEntityTypeDescription("myOtherDependentTable")).thenReturn(
				otherRefEntityTypeDescription);
		postgreSqlExceptionTranslator = new PostgreSqlExceptionTranslator(dataSource, entityTypeRegistry);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void PostgreSqlExceptionTranslator()
	{
		new PostgreSqlExceptionTranslator(null, null);
	}

	@Test
	public void translateValueTooLongViolation()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getMessage()).thenReturn("ERROR: value too long for type character varying(255)");
		//noinspection ThrowableResultOfMethodCallIgnored
		MolgenisValidationException e = postgreSqlExceptionTranslator.translateValueTooLongViolation();
		assertEquals(e.getMessage(), "One of the values being added is too long.");
	}

	@Test
	public void translateReadonlyViolation()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getMessage()).thenReturn(
				"Updating read-only column \"myColumn\" of table \"myTable\" with id [abc] is not allowed");
		//noinspection ThrowableResultOfMethodCallIgnored
		MolgenisValidationException e = postgreSqlExceptionTranslator.translateReadonlyViolation(
				new PSQLException(serverErrorMessage));
		assertEquals(e.getMessage(),
				"Updating read-only attribute 'myAttr' of entity 'myEntity' with id 'abc' is not allowed.");
	}

	@Test
	public void translateReadonlyViolationNoDoubleQuotes()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getMessage()).thenReturn(
				"Updating read-only column myColumn of table myTable with id [abc] is not allowed");
		//noinspection ThrowableResultOfMethodCallIgnored
		MolgenisValidationException e = postgreSqlExceptionTranslator.translateReadonlyViolation(
				new PSQLException(serverErrorMessage));
		assertEquals(e.getMessage(),
				"Updating read-only attribute 'myAttr' of entity 'myEntity' with id 'abc' is not allowed.");
	}

	@Test
	public void translateDependentObjectsStillExistOneDependentTableSingleDependency()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getSQLState()).thenReturn("2BP01");
		when(serverErrorMessage.getDetail()).thenReturn(
				"constraint my_foreign_key_constraint on table \"myTable\" depends on table \"myDependentTable\"");
		//noinspection ThrowableResultOfMethodCallIgnored
		MolgenisValidationException e = postgreSqlExceptionTranslator.translateDependentObjectsStillExist(
				new PSQLException(serverErrorMessage));
		assertEquals(e.getMessage(), "Cannot delete entity 'myRefEntity' because entity 'myEntity' depends on it.");
	}

	@Test
	public void translateDependentObjectsStillExistOneDependentTableMultipleDependencies()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getSQLState()).thenReturn("2BP01");
		when(serverErrorMessage.getDetail()).thenReturn(
				"constraint my_foreign_key_constraint on table \"myTable\" depends on table \"myDependentTable\"\nconstraint myOther_foreign_key_constraint on table \"myTable\" depends on table \"myDependentTable\"");
		//noinspection ThrowableResultOfMethodCallIgnored
		MolgenisValidationException e = postgreSqlExceptionTranslator.translateDependentObjectsStillExist(
				new PSQLException(serverErrorMessage));
		assertEquals(e.getMessage(), "Cannot delete entity 'myRefEntity' because entity 'myEntity' depends on it.");
	}

	@Test
	public void translateDependentObjectsStillExistMultipleDependentTables()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getSQLState()).thenReturn("2BP01");
		when(serverErrorMessage.getDetail()).thenReturn(
				"constraint my_foreign_key_constraint on table \"myTable\" depends on table \"myDependentTable\"\nconstraint myOther_foreign_key_constraint on table \"myTable\" depends on table \"myOtherDependentTable\"");
		//noinspection ThrowableResultOfMethodCallIgnored
		MolgenisValidationException e = postgreSqlExceptionTranslator.translateDependentObjectsStillExist(
				new PSQLException(serverErrorMessage));
		assertEquals(e.getMessage(),
				"Cannot delete entity 'myRefEntity' because entity 'myEntity' depends on it..Cannot delete entity 'myOtherRefEntity' because entity 'myEntity' depends on it.");
	}

	@Test
	public void translateDependentObjectsStillExistNoDoubleQuotes()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getSQLState()).thenReturn("2BP01");
		when(serverErrorMessage.getDetail()).thenReturn(
				"constraint my_foreign_key_constraint on table myTable depends on table myDependentTable");
		//noinspection ThrowableResultOfMethodCallIgnored
		MolgenisValidationException e = postgreSqlExceptionTranslator.translateDependentObjectsStillExist(
				new PSQLException(serverErrorMessage));
		assertEquals(e.getMessage(), "Cannot delete entity 'myRefEntity' because entity 'myEntity' depends on it.");
	}

	@Test
	public void translateNotNullViolation()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getSQLState()).thenReturn("23502");
		when(serverErrorMessage.getTable()).thenReturn("myTable");
		when(serverErrorMessage.getMessage()).thenReturn(
				"null value in column \"myColumn\" violates not-null constraint");
		//noinspection ThrowableResultOfMethodCallIgnored
		MolgenisValidationException e = postgreSqlExceptionTranslator.translateNotNullViolation(
				new PSQLException(serverErrorMessage));
		assertEquals(e.getMessage(), "The attribute 'myAttr' of entity 'myEntity' can not be null.");
	}

	@Test(expectedExceptions = RuntimeException.class)
	public void translateNotNullViolationBadMessage()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getSQLState()).thenReturn("23502");
		when(serverErrorMessage.getTable()).thenReturn("mytable");
		when(serverErrorMessage.getMessage()).thenReturn("xxxyyyzzzz");
		//noinspection ThrowableResultOfMethodCallIgnored
		postgreSqlExceptionTranslator.translateNotNullViolation(new PSQLException(serverErrorMessage));
	}

	@Test
	public void translateNotNullViolationNoDoubleQuotes()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getSQLState()).thenReturn("23502");
		when(serverErrorMessage.getTable()).thenReturn("myTable");
		when(serverErrorMessage.getMessage()).thenReturn("null value in column myColumn violates not-null constraint");
		//noinspection ThrowableResultOfMethodCallIgnored
		MolgenisValidationException e = postgreSqlExceptionTranslator.translateNotNullViolation(
				new PSQLException(serverErrorMessage));
		assertEquals(e.getMessage(), "The attribute 'myAttr' of entity 'myEntity' can not be null.");
	}

	@Test
	public void translateForeignKeyViolation()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getSQLState()).thenReturn("23503");
		when(serverErrorMessage.getTable()).thenReturn("myTable");
		when(serverErrorMessage.getDetail()).thenReturn("... (myColumn) ... (myValue) ...");
		//noinspection ThrowableResultOfMethodCallIgnored
		MolgenisValidationException e = postgreSqlExceptionTranslator.translateForeignKeyViolation(
				new PSQLException(serverErrorMessage));
		assertEquals(e.getMessage(), "Unknown xref value 'myValue' for attribute 'myAttr' of entity 'myEntity'.");
	}

	@Test
	public void translateForeignKeyViolationNotPresent()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getSQLState()).thenReturn("23503");
		when(serverErrorMessage.getTable()).thenReturn("myTable");
		when(serverErrorMessage.getDetail()).thenReturn("Key (myColumn)=(myValue) is not present in table \"myTable\"");
		//noinspection ThrowableResultOfMethodCallIgnored
		MolgenisValidationException e = postgreSqlExceptionTranslator.translateForeignKeyViolation(
				new PSQLException(serverErrorMessage));
		assertEquals(e.getMessage(), "Unknown xref value 'myValue' for attribute 'myAttr' of entity 'myEntity'.");
	}

	@Test
	public void translateForeignKeyViolationStillReferenced()
	{

		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getSQLState()).thenReturn("23503");
		when(serverErrorMessage.getTable()).thenReturn("myTable");
		when(serverErrorMessage.getMessage()).thenReturn(
				"update or delete on table \"myDependentTable\" violates foreign key constraint \"myTable_myAttr_fkey\" on table \"myTable\"");
		when(serverErrorMessage.getDetail()).thenReturn(
				"Key (myColumn)=(myValue) is still referenced from table \"myTable\"");
		//noinspection ThrowableResultOfMethodCallIgnored
		MolgenisValidationException e = postgreSqlExceptionTranslator.translateForeignKeyViolation(
				new PSQLException(serverErrorMessage));
		assertEquals(e.getMessage(), "Value 'myValue' for attribute 'myAttr' is referenced by entity 'myEntity'.");
	}

	@Test(expectedExceptions = RuntimeException.class)
	public void translateForeignKeyViolationBadMessage()
	{
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
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getSQLState()).thenReturn("23505");
		when(serverErrorMessage.getTable()).thenReturn("myTable");
		when(serverErrorMessage.getDetail()).thenReturn("Key (myColumn)=(myValue) already exists.");
		//noinspection ThrowableResultOfMethodCallIgnored
		MolgenisValidationException e = postgreSqlExceptionTranslator.translateUniqueKeyViolation(
				new PSQLException(serverErrorMessage));
		assertEquals(e.getMessage(), "Duplicate value 'myValue' for unique attribute 'myAttr' from entity 'myEntity'.");
	}

	@Test
	public void translateUniqueKeyViolationDoubleQuotes()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getSQLState()).thenReturn("23505");
		when(serverErrorMessage.getTable()).thenReturn("myTable");
		when(serverErrorMessage.getDetail()).thenReturn("Key (\"myColumn\")=(myValue) already exists.");
		//noinspection ThrowableResultOfMethodCallIgnored
		MolgenisValidationException e = postgreSqlExceptionTranslator.translateUniqueKeyViolation(
				new PSQLException(serverErrorMessage));
		assertEquals(e.getMessage(), "Duplicate value 'myValue' for unique attribute 'myAttr' from entity 'myEntity'.");
	}

	@Test
	public void translateUniqueKeyViolationCompositeKey()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getSQLState()).thenReturn("23505");
		when(serverErrorMessage.getTable()).thenReturn("myTable");
		when(serverErrorMessage.getDetail()).thenReturn(
				"Key (myIdColumn, myColumn)=(myIdValue, myValue) already exists.");
		//noinspection ThrowableResultOfMethodCallIgnored
		MolgenisValidationException e = postgreSqlExceptionTranslator.translateUniqueKeyViolation(
				new PSQLException(serverErrorMessage));
		assertEquals(e.getMessage(),
				"Duplicate list value 'myValue' for attribute 'myAttr' from entity 'myEntity' with id 'myIdValue'.");
	}

	@Test
	public void translateUniqueKeyViolationKeyIsDuplicated()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getSQLState()).thenReturn("23505");
		when(serverErrorMessage.getTable()).thenReturn("myTable");
		when(serverErrorMessage.getDetail()).thenReturn("Key (myColumn)=(myValue) is duplicated.");
		//noinspection ThrowableResultOfMethodCallIgnored
		MolgenisValidationException e = postgreSqlExceptionTranslator.translateUniqueKeyViolation(
				new PSQLException(serverErrorMessage));
		assertEquals(e.getMessage(), "The attribute 'myAttr' of entity 'myEntity' contains duplicate value 'myValue'.");
	}

	@Test
	public void translateUniqueKeyViolationKeyIsDuplicatedDoubleQuotes()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getSQLState()).thenReturn("23505");
		when(serverErrorMessage.getTable()).thenReturn("myTable");
		when(serverErrorMessage.getDetail()).thenReturn("Key (\"myColumn\")=(myValue) is duplicated.");
		//noinspection ThrowableResultOfMethodCallIgnored
		MolgenisValidationException e = postgreSqlExceptionTranslator.translateUniqueKeyViolation(
				new PSQLException(serverErrorMessage));
		assertEquals(e.getMessage(), "The attribute 'myAttr' of entity 'myEntity' contains duplicate value 'myValue'.");
	}

	@Test(expectedExceptions = RuntimeException.class)
	public void translateUniqueKeyViolationBadMessage()
	{
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
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getMessage()).thenReturn("invalid input syntax for integer: \"str1\"");
		//noinspection ThrowableResultOfMethodCallIgnored
		MolgenisValidationException e = PostgreSqlExceptionTranslator.translateInvalidIntegerException(
				new PSQLException(serverErrorMessage));
		assertEquals(e.getMessage(), "Value [str1] of this entity attribute is not of type [INT or LONG].");
	}

	@Test
	public void translateInvalidIntegerExceptionBoolean()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getMessage()).thenReturn("invalid input syntax for type boolean: \"str1\"");
		//noinspection ThrowableResultOfMethodCallIgnored
		MolgenisValidationException e = PostgreSqlExceptionTranslator.translateInvalidIntegerException(
				new PSQLException(serverErrorMessage));
		assertEquals(e.getMessage(), "Value [str1] of this entity attribute is not of type [BOOL].");
	}

	@Test
	public void translateInvalidIntegerExceptionDouble()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getMessage()).thenReturn("invalid input syntax for type double precision: \"str1\"");
		//noinspection ThrowableResultOfMethodCallIgnored
		MolgenisValidationException e = PostgreSqlExceptionTranslator.translateInvalidIntegerException(
				new PSQLException(serverErrorMessage));
		assertEquals(e.getMessage(), "Value [str1] of this entity attribute is not of type [DECIMAL].");
	}

	@Test
	public void translateInvalidIntegerExceptionDate()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getMessage()).thenReturn("invalid input syntax for type date: \"str1\"");
		//noinspection ThrowableResultOfMethodCallIgnored
		MolgenisValidationException e = PostgreSqlExceptionTranslator.translateInvalidIntegerException(
				new PSQLException(serverErrorMessage));
		assertEquals(e.getMessage(), "Value [str1] of this entity attribute is not of type [DATE].");
	}

	@Test
	public void translateInvalidIntegerExceptionDateTime()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getMessage()).thenReturn(
				"invalid input syntax for type timestamp with time zone: \"str1\"");
		//noinspection ThrowableResultOfMethodCallIgnored
		MolgenisValidationException e = PostgreSqlExceptionTranslator.translateInvalidIntegerException(
				new PSQLException(serverErrorMessage));
		assertEquals(e.getMessage(), "Value [str1] of this entity attribute is not of type [DATE_TIME].");
	}

	@Test
	public void translateCheckConstraintViolation()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getTable()).thenReturn("myTable");
		when(serverErrorMessage.getConstraint()).thenReturn("myTable_myColumn_chk");
		//noinspection ThrowableResultOfMethodCallIgnored
		MolgenisValidationException e = postgreSqlExceptionTranslator.translateCheckConstraintViolation(
				new PSQLException(serverErrorMessage));
		assertEquals(e.getMessage(), "Unknown enum value for attribute 'myAttr' of entity 'myEntity'.");
	}

	@Test
	public void translateUndefinedColumnException()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getSQLState()).thenReturn("42703");
		when(serverErrorMessage.getMessage()).thenReturn("Undefined column: 7 ERROR: column \"test\" does not exist");
		//noinspection ThrowableResultOfMethodCallIgnored
		MolgenisValidationException e = PostgreSqlExceptionTranslator.translateUndefinedColumnException(
				new PSQLException(serverErrorMessage));
		assertEquals(e.getMessage(), "Undefined column: 7 ERROR: column \"test\" does not exist");
	}
}
