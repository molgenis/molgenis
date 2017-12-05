package org.molgenis.data.postgresql;

import com.google.common.collect.ImmutableMap;
import org.molgenis.data.ErrorCodedDataAccessException;
import org.molgenis.data.UnknownDataAccessException;
import org.molgenis.data.postgresql.identifier.AttributeDescription;
import org.molgenis.data.postgresql.identifier.EntityTypeDescription;
import org.molgenis.data.postgresql.identifier.EntityTypeRegistry;
import org.molgenis.data.validation.*;
import org.postgresql.util.PSQLException;
import org.postgresql.util.ServerErrorMessage;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.sql.DataSource;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.data.postgresql.identifier.EntityTypeDescription.create;

public class PostgreSqlExceptionTranslatorTest
{

	private PostgreSqlExceptionTranslator postgreSqlExceptionTranslator;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		DataSource dataSource = mock(DataSource.class);
		EntityTypeRegistry entityTypeRegistry = mock(EntityTypeRegistry.class);
		EntityTypeDescription entityTypeDescription = create("myEntity",
				ImmutableMap.<String, AttributeDescription>builder().put("myColumn",
						AttributeDescription.create("myAttr")).build());
		EntityTypeDescription refEntityTypeDescription = create("myRefEntity",
				ImmutableMap.<String, AttributeDescription>builder().put("myColumn",
						AttributeDescription.create("myAttr")).build());
		EntityTypeDescription otherRefEntityTypeDescription = create("myOtherRefEntity",
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

	@Test(expectedExceptions = ReadOnlyConstraintViolationException.class, expectedExceptionsMessageRegExp = "type:myEntity attribute:myAttr value:abc")
	public void translateReadonlyViolation()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getMessage()).thenReturn(
				"Updating read-only column \"myColumn\" of table \"myTable\" with id [abc] is not allowed");
		//noinspection ThrowableResultOfMethodCallIgnored
		DataIntegrityViolationException e = postgreSqlExceptionTranslator.translateReadonlyViolation(
				new PSQLException(serverErrorMessage));
		throw e;
	}

	@Test(expectedExceptions = ReadOnlyConstraintViolationException.class, expectedExceptionsMessageRegExp = "type:myEntity attribute:myAttr value:abc")
	public void translateReadonlyViolationNoDoubleQuotes()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getMessage()).thenReturn(
				"Updating read-only column myColumn of table myTable with id [abc] is not allowed");
		//noinspection ThrowableResultOfMethodCallIgnored
		DataIntegrityViolationException e = postgreSqlExceptionTranslator.translateReadonlyViolation(
				new PSQLException(serverErrorMessage));
		throw e;
	}

	@Test(expectedExceptions = EntityTypeReferenceConstraintViolationException.class, expectedExceptionsMessageRegExp = "type:myRefEntity dependencies:\\[myEntity\\]")
	public void translateDependentObjectsStillExistOneDependentTableSingleDependency()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getSQLState()).thenReturn("2BP01");
		when(serverErrorMessage.getDetail()).thenReturn(
				"constraint my_foreign_key_constraint on table \"myTable\" depends on table \"myDependentTable\"");
		//noinspection ThrowableResultOfMethodCallIgnored
		DataIntegrityViolationException e = postgreSqlExceptionTranslator.translateDependentObjectsStillExist(
				new PSQLException(serverErrorMessage));
		throw e;
	}

	@Test(expectedExceptions = EntityTypeReferenceConstraintViolationException.class, expectedExceptionsMessageRegExp = "type:myRefEntity dependencies:\\[myEntity\\]")
	public void translateDependentObjectsStillExistOneDependentTableMultipleDependencies()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getSQLState()).thenReturn("2BP01");
		when(serverErrorMessage.getDetail()).thenReturn(
				"constraint my_foreign_key_constraint on table \"myTable\" depends on table \"myDependentTable\"\nconstraint myOther_foreign_key_constraint on table \"myTable\" depends on table \"myDependentTable\"");
		//noinspection ThrowableResultOfMethodCallIgnored
		DataIntegrityViolationException e = postgreSqlExceptionTranslator.translateDependentObjectsStillExist(
				new PSQLException(serverErrorMessage));
		throw e;
	}

	@Test(expectedExceptions = EntityTypeReferenceConstraintViolationException.class, expectedExceptionsMessageRegExp = "type:myRefEntity dependencies:\\[myEntity\\],type:myOtherRefEntity dependencies:\\[myEntity\\]")
	public void translateDependentObjectsStillExistMultipleDependentTables()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getSQLState()).thenReturn("2BP01");
		when(serverErrorMessage.getDetail()).thenReturn(
				"constraint my_foreign_key_constraint on table \"myTable\" depends on table \"myDependentTable\"\nconstraint myOther_foreign_key_constraint on table \"myTable\" depends on table \"myOtherDependentTable\"");
		//noinspection ThrowableResultOfMethodCallIgnored
		DataIntegrityViolationException e = postgreSqlExceptionTranslator.translateDependentObjectsStillExist(
				new PSQLException(serverErrorMessage));
		throw e;
	}

	@Test(expectedExceptions = EntityTypeReferenceConstraintViolationException.class, expectedExceptionsMessageRegExp = "type:myRefEntity dependencies:\\[myEntity\\]")
	public void translateDependentObjectsStillExistNoDoubleQuotes()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getSQLState()).thenReturn("2BP01");
		when(serverErrorMessage.getDetail()).thenReturn(
				"constraint my_foreign_key_constraint on table myTable depends on table myDependentTable");
		//noinspection ThrowableResultOfMethodCallIgnored
		DataIntegrityViolationException e = postgreSqlExceptionTranslator.translateDependentObjectsStillExist(
				new PSQLException(serverErrorMessage));
		throw e;
	}

	@Test(expectedExceptions = NotNullConstraintViolationException.class, expectedExceptionsMessageRegExp = "type:myEntity attribute:myAttr entityId:null")
	public void translateNotNullViolation()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getSQLState()).thenReturn("23502");
		when(serverErrorMessage.getTable()).thenReturn("myTable");
		when(serverErrorMessage.getMessage()).thenReturn(
				"null value in column \"myColumn\" violates not-null constraint");
		//noinspection ThrowableResultOfMethodCallIgnored
		DataIntegrityViolationException e = postgreSqlExceptionTranslator.translateNotNullViolation(
				new PSQLException(serverErrorMessage));
		throw e; // TODO update test based upon discussion result in NotNullConstraintViolationException
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "Error translating exception")
	public void translateNotNullViolationBadMessage()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getSQLState()).thenReturn("23502");
		when(serverErrorMessage.getTable()).thenReturn("mytable");
		when(serverErrorMessage.getMessage()).thenReturn("xxxyyyzzzz");
		//noinspection ThrowableResultOfMethodCallIgnored
		postgreSqlExceptionTranslator.translateNotNullViolation(new PSQLException(serverErrorMessage));
	}

	@Test(expectedExceptions = NotNullConstraintViolationException.class, expectedExceptionsMessageRegExp = "type:myEntity attribute:myAttr entityId:null")
	public void translateNotNullViolationNoDoubleQuotes()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getSQLState()).thenReturn("23502");
		when(serverErrorMessage.getTable()).thenReturn("myTable");
		when(serverErrorMessage.getMessage()).thenReturn("null value in column myColumn violates not-null constraint");
		//noinspection ThrowableResultOfMethodCallIgnored
		DataIntegrityViolationException e = postgreSqlExceptionTranslator.translateNotNullViolation(
				new PSQLException(serverErrorMessage));
		throw e;
	}

	@Test(expectedExceptions = EntityReferenceUnknownConstraintViolationException.class, expectedExceptionsMessageRegExp = "type:myEntity attribute:myAttr value: myValue")
	public void translateForeignKeyViolation()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getSQLState()).thenReturn("23503");
		when(serverErrorMessage.getTable()).thenReturn("myTable");
		when(serverErrorMessage.getDetail()).thenReturn("... (myColumn) ... (myValue) ...");
		//noinspection ThrowableResultOfMethodCallIgnored
		DataIntegrityViolationException e = postgreSqlExceptionTranslator.translateForeignKeyViolation(
				new PSQLException(serverErrorMessage));
		throw e;
	}

	@Test(expectedExceptions = EntityReferenceUnknownConstraintViolationException.class, expectedExceptionsMessageRegExp = "type:myEntity attribute:myAttr value: myValue")
	public void translateForeignKeyViolationNotPresent()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getSQLState()).thenReturn("23503");
		when(serverErrorMessage.getTable()).thenReturn("myTable");
		when(serverErrorMessage.getDetail()).thenReturn("Key (myColumn)=(myValue) is not present in table \"myTable\"");
		//noinspection ThrowableResultOfMethodCallIgnored
		DataIntegrityViolationException e = postgreSqlExceptionTranslator.translateForeignKeyViolation(
				new PSQLException(serverErrorMessage));
		throw e;
	}

	@Test(expectedExceptions = EntityReferenceConstraintViolationException.class, expectedExceptionsMessageRegExp = "type:myEntity attribute:myAttr value:myValue")
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
		DataIntegrityViolationException e = postgreSqlExceptionTranslator.translateForeignKeyViolation(
				new PSQLException(serverErrorMessage));
		throw e;
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "Error translating exception")
	public void translateForeignKeyViolationBadMessage()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getSQLState()).thenReturn("23503");
		when(serverErrorMessage.getTable()).thenReturn("mytable");
		when(serverErrorMessage.getDetail()).thenReturn("xxxyyyyzzzz");
		//noinspection ThrowableResultOfMethodCallIgnored
		postgreSqlExceptionTranslator.translateForeignKeyViolation(new PSQLException(serverErrorMessage));
	}

	@Test(expectedExceptions = UniqueConstraintViolationException.class, expectedExceptionsMessageRegExp = "type:myEntity attribute:myAttr value:myValue")
	public void translateUniqueKeyViolation()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getSQLState()).thenReturn("23505");
		when(serverErrorMessage.getTable()).thenReturn("myTable");
		when(serverErrorMessage.getDetail()).thenReturn("Key (myColumn)=(myValue) already exists.");
		//noinspection ThrowableResultOfMethodCallIgnored
		DataIntegrityViolationException e = postgreSqlExceptionTranslator.translateUniqueKeyViolation(
				new PSQLException(serverErrorMessage));
		throw e;
	}

	@Test(expectedExceptions = UniqueConstraintViolationException.class, expectedExceptionsMessageRegExp = "type:myEntity attribute:myAttr value:myValue")
	public void translateUniqueKeyViolationDoubleQuotes()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getSQLState()).thenReturn("23505");
		when(serverErrorMessage.getTable()).thenReturn("myTable");
		when(serverErrorMessage.getDetail()).thenReturn("Key (\"myColumn\")=(myValue) already exists.");
		//noinspection ThrowableResultOfMethodCallIgnored
		DataIntegrityViolationException e = postgreSqlExceptionTranslator.translateUniqueKeyViolation(
				new PSQLException(serverErrorMessage));
		throw e;
	}

	@Test(expectedExceptions = UniqueReferenceConstraintViolationException.class, expectedExceptionsMessageRegExp = "type:myEntity attribute:myAttr entity:myValue value:myIdValue")
	public void translateUniqueKeyViolationCompositeKey()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getSQLState()).thenReturn("23505");
		when(serverErrorMessage.getTable()).thenReturn("myTable");
		when(serverErrorMessage.getDetail()).thenReturn(
				"Key (myIdColumn, myColumn)=(myIdValue, myValue) already exists.");
		//noinspection ThrowableResultOfMethodCallIgnored
		DataIntegrityViolationException e = postgreSqlExceptionTranslator.translateUniqueKeyViolation(
				new PSQLException(serverErrorMessage));
		throw e;
	}

	@Test(expectedExceptions = UniqueConstraintCreationException.class, expectedExceptionsMessageRegExp = "type:myEntity attribute:myAttr value:myValue")
	public void translateUniqueKeyViolationKeyIsDuplicated()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getSQLState()).thenReturn("23505");
		when(serverErrorMessage.getTable()).thenReturn("myTable");
		when(serverErrorMessage.getDetail()).thenReturn("Key (myColumn)=(myValue) is duplicated.");
		//noinspection ThrowableResultOfMethodCallIgnored
		DataIntegrityViolationException e = postgreSqlExceptionTranslator.translateUniqueKeyViolation(
				new PSQLException(serverErrorMessage));
		throw e;
	}

	@Test(expectedExceptions = UniqueConstraintCreationException.class, expectedExceptionsMessageRegExp = "type:myEntity attribute:myAttr value:myValue")
	public void translateUniqueKeyViolationKeyIsDuplicatedDoubleQuotes()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getSQLState()).thenReturn("23505");
		when(serverErrorMessage.getTable()).thenReturn("myTable");
		when(serverErrorMessage.getDetail()).thenReturn("Key (\"myColumn\")=(myValue) is duplicated.");
		//noinspection ThrowableResultOfMethodCallIgnored
		DataIntegrityViolationException e = postgreSqlExceptionTranslator.translateUniqueKeyViolation(
				new PSQLException(serverErrorMessage));
		throw e;
	}

	@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "Error translating exception")
	public void translateUniqueKeyViolationBadMessage()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getSQLState()).thenReturn("23505");
		when(serverErrorMessage.getTable()).thenReturn("mytable");
		when(serverErrorMessage.getDetail()).thenReturn("xxxyyyyzzz");
		//noinspection ThrowableResultOfMethodCallIgnored
		postgreSqlExceptionTranslator.translateUniqueKeyViolation(new PSQLException(serverErrorMessage));
	}

	@Test(expectedExceptions = DataTypeConstraintViolationException.class, expectedExceptionsMessageRegExp = "type:INT or LONG value:str1")
	public void translateInvalidIntegerExceptionInteger()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getMessage()).thenReturn("invalid input syntax for integer: \"str1\"");
		//noinspection ThrowableResultOfMethodCallIgnored
		DataIntegrityViolationException e = PostgreSqlExceptionTranslator.translateInvalidIntegerException(
				new PSQLException(serverErrorMessage));
		throw e; // TODO updated based on discussion result in DataTypeConstraintViolationException
	}

	@Test(expectedExceptions = DataTypeConstraintViolationException.class, expectedExceptionsMessageRegExp = "type:BOOL value:str1")
	public void translateInvalidIntegerExceptionBoolean()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getMessage()).thenReturn("invalid input syntax for type boolean: \"str1\"");
		//noinspection ThrowableResultOfMethodCallIgnored
		DataIntegrityViolationException e = PostgreSqlExceptionTranslator.translateInvalidIntegerException(
				new PSQLException(serverErrorMessage));
		throw e;
	}

	@Test(expectedExceptions = DataTypeConstraintViolationException.class, expectedExceptionsMessageRegExp = "type:DECIMAL value:str1")
	public void translateInvalidIntegerExceptionDouble()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getMessage()).thenReturn("invalid input syntax for type double precision: \"str1\"");
		//noinspection ThrowableResultOfMethodCallIgnored
		DataIntegrityViolationException e = PostgreSqlExceptionTranslator.translateInvalidIntegerException(
				new PSQLException(serverErrorMessage));
		throw e;
	}

	@Test(expectedExceptions = DataTypeConstraintViolationException.class, expectedExceptionsMessageRegExp = "type:DATE value:str1")
	public void translateInvalidIntegerExceptionDate()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getMessage()).thenReturn("invalid input syntax for type date: \"str1\"");
		//noinspection ThrowableResultOfMethodCallIgnored
		DataIntegrityViolationException e = PostgreSqlExceptionTranslator.translateInvalidIntegerException(
				new PSQLException(serverErrorMessage));
		throw e;
	}

	@Test(expectedExceptions = DataTypeConstraintViolationException.class, expectedExceptionsMessageRegExp = "type:DATE_TIME value:str1")
	public void translateInvalidIntegerExceptionDateTime()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getMessage()).thenReturn(
				"invalid input syntax for type timestamp with time zone: \"str1\"");
		//noinspection ThrowableResultOfMethodCallIgnored
		DataIntegrityViolationException e = PostgreSqlExceptionTranslator.translateInvalidIntegerException(
				new PSQLException(serverErrorMessage));
		throw e;
	}

	@Test(expectedExceptions = EnumConstraintModificationException.class, expectedExceptionsMessageRegExp = "type:myEntity")
	public void translateCheckConstraintViolation()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getTable()).thenReturn("myTable");
		when(serverErrorMessage.getConstraint()).thenReturn("myTable_myColumn_chk");
		//noinspection ThrowableResultOfMethodCallIgnored
		DataIntegrityViolationException e = postgreSqlExceptionTranslator.translateCheckConstraintViolation(
				new PSQLException(serverErrorMessage));
		throw e;
	}

	@Test(expectedExceptions = UnknownDataAccessException.class, expectedExceptionsMessageRegExp = "unknown error")
	public void translateUndefinedColumnException()
	{
		ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
		when(serverErrorMessage.getSQLState()).thenReturn("42703");
		when(serverErrorMessage.getMessage()).thenReturn("Undefined column: 7 ERROR: column \"test\" does not exist");
		//noinspection ThrowableResultOfMethodCallIgnored
		ErrorCodedDataAccessException e = PostgreSqlExceptionTranslator.translateUndefinedColumnException(
				new PSQLException(serverErrorMessage));
		throw e;
	}
}
