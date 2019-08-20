package org.molgenis.data.postgresql;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import javax.sql.DataSource;
import org.molgenis.data.DuplicateValueException;
import org.molgenis.data.EntityTypeReferencedException;
import org.molgenis.data.InvalidValueTypeException;
import org.molgenis.data.ListValueAlreadyExistsException;
import org.molgenis.data.ReadonlyValueException;
import org.molgenis.data.UnknownEnumValueException;
import org.molgenis.data.UnknownValueReferenceException;
import org.molgenis.data.ValueAlreadyExistsException;
import org.molgenis.data.ValueLengthExceededException;
import org.molgenis.data.ValueReferencedException;
import org.molgenis.data.ValueRequiredException;
import org.molgenis.data.postgresql.identifier.AttributeDescription;
import org.molgenis.data.postgresql.identifier.EntityTypeDescription;
import org.molgenis.data.postgresql.identifier.EntityTypeRegistry;
import org.postgresql.util.PSQLException;
import org.postgresql.util.ServerErrorMessage;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PostgreSqlExceptionTranslatorTest {

  private PostgreSqlExceptionTranslator postgreSqlExceptionTranslator;

  @BeforeMethod
  public void setUpBeforeMethod() {
    DataSource dataSource = mock(DataSource.class);
    EntityTypeRegistry entityTypeRegistry = mock(EntityTypeRegistry.class);
    EntityTypeDescription entityTypeDescription =
        EntityTypeDescription.create(
            "myEntity",
            ImmutableMap.<String, AttributeDescription>builder()
                .put("myColumn", AttributeDescription.create("myAttr"))
                .build());
    EntityTypeDescription refEntityTypeDescription =
        EntityTypeDescription.create(
            "myRefEntity",
            ImmutableMap.<String, AttributeDescription>builder()
                .put("myColumn", AttributeDescription.create("myAttr"))
                .build());
    EntityTypeDescription otherRefEntityTypeDescription =
        EntityTypeDescription.create(
            "myOtherRefEntity",
            ImmutableMap.<String, AttributeDescription>builder()
                .put("myColumn", AttributeDescription.create("myAttr"))
                .build());

    when(entityTypeRegistry.getEntityTypeDescription("myTable")).thenReturn(entityTypeDescription);
    when(entityTypeRegistry.getEntityTypeDescription("myDependentTable"))
        .thenReturn(refEntityTypeDescription);
    when(entityTypeRegistry.getEntityTypeDescription("myOtherDependentTable"))
        .thenReturn(otherRefEntityTypeDescription);
    postgreSqlExceptionTranslator =
        new PostgreSqlExceptionTranslator(dataSource, entityTypeRegistry);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void PostgreSqlExceptionTranslator() {
    new PostgreSqlExceptionTranslator(null, null);
  }

  @Test
  public void translateValueTooLongViolation() {
    ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
    when(serverErrorMessage.getMessage())
        .thenReturn("ERROR: value too long for type character varying(255)");
    //noinspection ThrowableResultOfMethodCallIgnored
    ValueLengthExceededException e =
        postgreSqlExceptionTranslator.translateValueTooLongViolation(mock(Throwable.class));
    assertNull(e.getMessage());
  }

  @Test
  public void translateReadonlyViolation() {
    ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
    when(serverErrorMessage.getMessage())
        .thenReturn(
            "Updating read-only column \"myColumn\" of table \"myTable\" with id [abc] is not allowed");
    //noinspection ThrowableResultOfMethodCallIgnored
    ReadonlyValueException e =
        postgreSqlExceptionTranslator.translateReadonlyViolation(
            mock(Throwable.class), new PSQLException(serverErrorMessage));
    assertEquals(e.getMessage(), "entityTypeId:myEntity attributeName:myAttr entityId:abc");
  }

  @Test
  public void translateReadonlyViolationNoDoubleQuotes() {
    ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
    when(serverErrorMessage.getMessage())
        .thenReturn(
            "Updating read-only column myColumn of table myTable with id [abc] is not allowed");
    //noinspection ThrowableResultOfMethodCallIgnored

    ReadonlyValueException e =
        postgreSqlExceptionTranslator.translateReadonlyViolation(
            mock(Throwable.class), new PSQLException(serverErrorMessage));
    assertEquals(e.getMessage(), "entityTypeId:myEntity attributeName:myAttr entityId:abc");
  }

  @Test
  public void translateDependentObjectsStillExistOneDependentTableSingleDependency() {
    ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
    when(serverErrorMessage.getSQLState()).thenReturn("2BP01");
    when(serverErrorMessage.getDetail())
        .thenReturn(
            "constraint my_foreign_key_constraint on table \"myTable\" depends on table \"myDependentTable\"");
    //noinspection ThrowableResultOfMethodCallIgnored

    EntityTypeReferencedException e =
        postgreSqlExceptionTranslator.translateDependentObjectsStillExist(
            mock(Throwable.class), new PSQLException(serverErrorMessage));
    assertEquals(e.getMessage(), "dependencies:myRefEntity=myEntity");
  }

  @Test
  public void translateDependentObjectsStillExistOneDependentTableMultipleDependencies() {
    ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
    when(serverErrorMessage.getSQLState()).thenReturn("2BP01");
    when(serverErrorMessage.getDetail())
        .thenReturn(
            "constraint my_foreign_key_constraint on table \"myTable\" depends on table \"myDependentTable\"\nconstraint myOther_foreign_key_constraint on table \"myTable\" depends on table \"myDependentTable\"");
    //noinspection ThrowableResultOfMethodCallIgnored

    EntityTypeReferencedException e =
        postgreSqlExceptionTranslator.translateDependentObjectsStillExist(
            mock(Throwable.class), new PSQLException(serverErrorMessage));
    assertEquals(e.getMessage(), "dependencies:myRefEntity=myEntity");
  }

  @Test
  public void translateDependentObjectsStillExistMultipleDependentTables() {
    ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
    when(serverErrorMessage.getSQLState()).thenReturn("2BP01");
    when(serverErrorMessage.getDetail())
        .thenReturn(
            "constraint my_foreign_key_constraint on table \"myTable\" depends on table \"myDependentTable\"\nconstraint myOther_foreign_key_constraint on table \"myTable\" depends on table \"myOtherDependentTable\"");
    //noinspection ThrowableResultOfMethodCallIgnored

    EntityTypeReferencedException e =
        postgreSqlExceptionTranslator.translateDependentObjectsStillExist(
            mock(Throwable.class), new PSQLException(serverErrorMessage));
    assertEquals(e.getMessage(), "dependencies:myRefEntity=myEntity;myOtherRefEntity=myEntity");
  }

  @Test
  public void translateDependentObjectsStillExistNoDoubleQuotes() {
    ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
    when(serverErrorMessage.getSQLState()).thenReturn("2BP01");
    when(serverErrorMessage.getDetail())
        .thenReturn(
            "constraint my_foreign_key_constraint on table myTable depends on table myDependentTable");
    //noinspection ThrowableResultOfMethodCallIgnored

    EntityTypeReferencedException e =
        postgreSqlExceptionTranslator.translateDependentObjectsStillExist(
            mock(Throwable.class), new PSQLException(serverErrorMessage));
    assertEquals(e.getMessage(), "dependencies:myRefEntity=myEntity");
  }

  @Test
  public void translateNotNullViolation() {
    ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
    when(serverErrorMessage.getSQLState()).thenReturn("23502");
    when(serverErrorMessage.getTable()).thenReturn("myTable");
    when(serverErrorMessage.getMessage())
        .thenReturn("null value in column \"myColumn\" violates not-null constraint");
    //noinspection ThrowableResultOfMethodCallIgnored

    Exception e =
        postgreSqlExceptionTranslator.translateNotNullViolation(
            mock(Throwable.class), new PSQLException(serverErrorMessage));
    assertEquals(e.getMessage(), "entityTypeId:myEntity attributeName:myAttr");
    assertTrue(e instanceof ValueRequiredException);
  }

  @Test(expectedExceptions = RuntimeException.class)
  public void translateNotNullViolationBadMessage() {
    ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
    when(serverErrorMessage.getSQLState()).thenReturn("23502");
    when(serverErrorMessage.getTable()).thenReturn("mytable");
    when(serverErrorMessage.getMessage()).thenReturn("xxxyyyzzzz");
    //noinspection ThrowableResultOfMethodCallIgnored
    postgreSqlExceptionTranslator.translateNotNullViolation(
        mock(Throwable.class), new PSQLException(serverErrorMessage));
  }

  @Test
  public void translateNotNullViolationNoDoubleQuotes() {
    ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
    when(serverErrorMessage.getSQLState()).thenReturn("23502");
    when(serverErrorMessage.getTable()).thenReturn("myTable");
    when(serverErrorMessage.getMessage())
        .thenReturn("null value in column myColumn violates not-null constraint");
    //noinspection ThrowableResultOfMethodCallIgnored

    Exception e =
        postgreSqlExceptionTranslator.translateNotNullViolation(
            mock(Throwable.class), new PSQLException(serverErrorMessage));
    assertEquals(e.getMessage(), "entityTypeId:myEntity attributeName:myAttr");
    assertTrue(e instanceof ValueRequiredException);
  }

  @Test
  public void translateForeignKeyViolation() {
    ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
    when(serverErrorMessage.getSQLState()).thenReturn("23503");
    when(serverErrorMessage.getTable()).thenReturn("myTable");
    when(serverErrorMessage.getDetail()).thenReturn("... (myColumn) ... (myValue) ...");
    //noinspection ThrowableResultOfMethodCallIgnored

    Exception e =
        postgreSqlExceptionTranslator.translateForeignKeyViolation(
            mock(Throwable.class), new PSQLException(serverErrorMessage));
    assertEquals(e.getMessage(), "entityTypeId:myEntity attributeName:myAttr value:myValue");
    assertTrue(e instanceof UnknownValueReferenceException);
  }

  @Test
  public void translateForeignKeyViolationNotPresent() {
    ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
    when(serverErrorMessage.getSQLState()).thenReturn("23503");
    when(serverErrorMessage.getTable()).thenReturn("myTable");
    when(serverErrorMessage.getDetail())
        .thenReturn("Key (myColumn)=(myValue) is not present in table \"myTable\"");
    //noinspection ThrowableResultOfMethodCallIgnored

    Exception e =
        postgreSqlExceptionTranslator.translateForeignKeyViolation(
            mock(Throwable.class), new PSQLException(serverErrorMessage));
    assertEquals(e.getMessage(), "entityTypeId:myEntity attributeName:myAttr value:myValue");
    assertTrue(e instanceof UnknownValueReferenceException);
  }

  @Test
  public void translateForeignKeyViolationStillReferenced() {
    ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
    when(serverErrorMessage.getSQLState()).thenReturn("23503");
    when(serverErrorMessage.getTable()).thenReturn("myTable");
    when(serverErrorMessage.getMessage())
        .thenReturn(
            "update or delete on table \"myDependentTable\" violates foreign key constraint \"myTable_myAttr_fkey\" on table \"myTable\"");
    when(serverErrorMessage.getDetail())
        .thenReturn("Key (myColumn)=(myValue) is still referenced from table \"myTable\"");
    //noinspection ThrowableResultOfMethodCallIgnored

    Exception e =
        postgreSqlExceptionTranslator.translateForeignKeyViolation(
            mock(Throwable.class), new PSQLException(serverErrorMessage));
    assertEquals(e.getMessage(), "entityTypeId:myEntity attributeName:myAttr value:myValue");
    assertTrue(e instanceof ValueReferencedException);
  }

  @Test(expectedExceptions = RuntimeException.class)
  public void translateForeignKeyViolationBadMessage() {
    ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
    when(serverErrorMessage.getSQLState()).thenReturn("23503");
    when(serverErrorMessage.getTable()).thenReturn("mytable");
    when(serverErrorMessage.getDetail()).thenReturn("xxxyyyyzzzz");
    //noinspection ThrowableResultOfMethodCallIgnored
    postgreSqlExceptionTranslator.translateForeignKeyViolation(
        mock(Throwable.class), new PSQLException(serverErrorMessage));
  }

  @Test
  public void translateForeignKeyViolationCannotResolveAttribute() {
    ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
    when(serverErrorMessage.getSQLState()).thenReturn("23503");
    when(serverErrorMessage.getTable()).thenReturn("myTable");
    when(serverErrorMessage.getDetail()).thenReturn("... (myUnknownColumn) ... (myValue) ...");
    //noinspection ThrowableResultOfMethodCallIgnored

    Exception e =
        postgreSqlExceptionTranslator.translateForeignKeyViolation(
            mock(Throwable.class), new PSQLException(serverErrorMessage));
    assertEquals(e.getMessage(), "entityTypeId:myEntity attributeName:null value:myValue");
    assertTrue(e instanceof UnknownValueReferenceException);
  }

  @Test
  public void translateForeignKeyViolationCannotResolveEntityType() {
    ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
    when(serverErrorMessage.getSQLState()).thenReturn("23503");
    when(serverErrorMessage.getTable()).thenReturn("myUnknownTable");
    when(serverErrorMessage.getDetail()).thenReturn("... (myColumn) ... (myValue) ...");
    //noinspection ThrowableResultOfMethodCallIgnored

    Exception e =
        postgreSqlExceptionTranslator.translateForeignKeyViolation(
            mock(Throwable.class), new PSQLException(serverErrorMessage));
    assertEquals(e.getMessage(), "entityTypeId:null attributeName:null value:myValue");
    assertTrue(e instanceof UnknownValueReferenceException);
  }

  @Test
  public void translateUniqueKeyViolation() {
    ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
    when(serverErrorMessage.getSQLState()).thenReturn("23505");
    when(serverErrorMessage.getTable()).thenReturn("myTable");
    when(serverErrorMessage.getDetail()).thenReturn("Key (myColumn)=(myValue) already exists.");
    //noinspection ThrowableResultOfMethodCallIgnored

    Exception e =
        postgreSqlExceptionTranslator.translateUniqueKeyViolation(
            mock(Throwable.class), new PSQLException(serverErrorMessage));
    assertEquals(e.getMessage(), "entityTypeId:myEntity attributeName:myAttr value:myValue");
    assertTrue(e instanceof ValueAlreadyExistsException);
  }

  @Test
  public void translateUniqueKeyViolationDoubleQuotes() {
    ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
    when(serverErrorMessage.getSQLState()).thenReturn("23505");
    when(serverErrorMessage.getTable()).thenReturn("myTable");
    when(serverErrorMessage.getDetail()).thenReturn("Key (\"myColumn\")=(myValue) already exists.");
    //noinspection ThrowableResultOfMethodCallIgnored

    Exception e =
        postgreSqlExceptionTranslator.translateUniqueKeyViolation(
            mock(Throwable.class), new PSQLException(serverErrorMessage));
    assertEquals(e.getMessage(), "entityTypeId:myEntity attributeName:myAttr value:myValue");
    assertTrue(e instanceof ValueAlreadyExistsException);
  }

  @Test
  public void translateUniqueKeyViolationCompositeKey() {
    ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
    when(serverErrorMessage.getSQLState()).thenReturn("23505");
    when(serverErrorMessage.getTable()).thenReturn("myTable");
    when(serverErrorMessage.getDetail())
        .thenReturn("Key (myIdColumn, myColumn)=(myIdValue, myValue) already exists.");
    //noinspection ThrowableResultOfMethodCallIgnored

    Exception e =
        postgreSqlExceptionTranslator.translateUniqueKeyViolation(
            mock(Throwable.class), new PSQLException(serverErrorMessage));
    assertEquals(
        e.getMessage(),
        "entityTypeId:myEntity attributeName:myAttr entityId:myIdValue value:myValue");
    assertTrue(e instanceof ListValueAlreadyExistsException);
  }

  @Test
  public void translateUniqueKeyViolationKeyIsDuplicated() {
    ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
    when(serverErrorMessage.getSQLState()).thenReturn("23505");
    when(serverErrorMessage.getTable()).thenReturn("myTable");
    when(serverErrorMessage.getDetail()).thenReturn("Key (myColumn)=(myValue) is duplicated.");
    //noinspection ThrowableResultOfMethodCallIgnored

    Exception e =
        postgreSqlExceptionTranslator.translateUniqueKeyViolation(
            mock(Throwable.class), new PSQLException(serverErrorMessage));
    assertEquals(e.getMessage(), "entityTypeId:myEntity attributeName:myAttr value:myValue");
    assertTrue(e instanceof DuplicateValueException);
  }

  @Test
  public void translateUniqueKeyViolationKeyIsDuplicatedDoubleQuotes() {
    ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
    when(serverErrorMessage.getSQLState()).thenReturn("23505");
    when(serverErrorMessage.getTable()).thenReturn("myTable");
    when(serverErrorMessage.getDetail()).thenReturn("Key (\"myColumn\")=(myValue) is duplicated.");
    //noinspection ThrowableResultOfMethodCallIgnored

    Exception e =
        postgreSqlExceptionTranslator.translateUniqueKeyViolation(
            mock(Throwable.class), new PSQLException(serverErrorMessage));
    assertEquals(e.getMessage(), "entityTypeId:myEntity attributeName:myAttr value:myValue");
    assertTrue(e instanceof DuplicateValueException);
  }

  @Test(expectedExceptions = RuntimeException.class)
  public void translateUniqueKeyViolationBadMessage() {
    ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
    when(serverErrorMessage.getSQLState()).thenReturn("23505");
    when(serverErrorMessage.getTable()).thenReturn("mytable");
    when(serverErrorMessage.getDetail()).thenReturn("xxxyyyyzzz");
    //noinspection ThrowableResultOfMethodCallIgnored
    postgreSqlExceptionTranslator.translateUniqueKeyViolation(
        mock(Throwable.class), new PSQLException(serverErrorMessage));
  }

  @Test
  public void translateInvalidIntegerExceptionInteger() {
    ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
    when(serverErrorMessage.getMessage()).thenReturn("invalid input syntax for integer: \"str1\"");
    //noinspection ThrowableResultOfMethodCallIgnored

    InvalidValueTypeException e =
        PostgreSqlExceptionTranslator.translateInvalidIntegerException(
            mock(Throwable.class), new PSQLException(serverErrorMessage));
    assertEquals(e.getMessage(), "value:str1 type:INT or LONG");
  }

  @Test
  public void translateInvalidIntegerExceptionBoolean() {
    ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
    when(serverErrorMessage.getMessage())
        .thenReturn("invalid input syntax for type boolean: \"str1\"");
    //noinspection ThrowableResultOfMethodCallIgnored

    InvalidValueTypeException e =
        PostgreSqlExceptionTranslator.translateInvalidIntegerException(
            mock(Throwable.class), new PSQLException(serverErrorMessage));
    assertEquals(e.getMessage(), "value:str1 type:BOOL");
  }

  @Test
  public void translateInvalidIntegerExceptionDouble() {
    ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
    when(serverErrorMessage.getMessage())
        .thenReturn("invalid input syntax for type double precision: \"str1\"");
    //noinspection ThrowableResultOfMethodCallIgnored

    InvalidValueTypeException e =
        PostgreSqlExceptionTranslator.translateInvalidIntegerException(
            mock(Throwable.class), new PSQLException(serverErrorMessage));
    assertEquals(e.getMessage(), "value:str1 type:DECIMAL");
  }

  @Test
  public void translateInvalidIntegerExceptionDate() {
    ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
    when(serverErrorMessage.getMessage())
        .thenReturn("invalid input syntax for type date: \"str1\"");
    //noinspection ThrowableResultOfMethodCallIgnored

    InvalidValueTypeException e =
        PostgreSqlExceptionTranslator.translateInvalidIntegerException(
            mock(Throwable.class), new PSQLException(serverErrorMessage));
    assertEquals(e.getMessage(), "value:str1 type:DATE");
  }

  @Test
  public void translateInvalidIntegerExceptionDateTime() {
    ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
    when(serverErrorMessage.getMessage())
        .thenReturn("invalid input syntax for type timestamp with time zone: \"str1\"");
    //noinspection ThrowableResultOfMethodCallIgnored

    InvalidValueTypeException e =
        PostgreSqlExceptionTranslator.translateInvalidIntegerException(
            mock(Throwable.class), new PSQLException(serverErrorMessage));
    assertEquals(e.getMessage(), "value:str1 type:DATE_TIME");
  }

  @Test
  public void translateCheckConstraintViolation() {
    ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
    when(serverErrorMessage.getTable()).thenReturn("myTable");
    when(serverErrorMessage.getConstraint()).thenReturn("myTable_myColumn_chk");
    //noinspection ThrowableResultOfMethodCallIgnored

    UnknownEnumValueException e =
        postgreSqlExceptionTranslator.translateCheckConstraintViolation(
            mock(Throwable.class), new PSQLException(serverErrorMessage));
    assertEquals(e.getMessage(), "entityTypeId:myEntity attributeName:myAttr");
  }

  @Test
  public void translateUndefinedColumnException() {
    ServerErrorMessage serverErrorMessage = mock(ServerErrorMessage.class);
    when(serverErrorMessage.getSQLState()).thenReturn("42703");
    when(serverErrorMessage.getMessage())
        .thenReturn("Undefined column: 7 ERROR: column \"test\" does not exist");
    //noinspection ThrowableResultOfMethodCallIgnored

    Exception e =
        PostgreSqlExceptionTranslator.translateUndefinedColumnException(
            mock(Throwable.class), new PSQLException(serverErrorMessage));
    assertEquals(e.getMessage(), "Undefined column: 7 ERROR: column \"test\" does not exist");
  }
}
