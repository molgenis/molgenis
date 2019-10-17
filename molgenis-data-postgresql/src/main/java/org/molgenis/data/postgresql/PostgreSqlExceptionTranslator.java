package org.molgenis.data.postgresql;

import static java.util.Collections.singleton;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.BOOL;
import static org.molgenis.data.meta.AttributeType.DATE;
import static org.molgenis.data.meta.AttributeType.DATE_TIME;
import static org.molgenis.data.meta.AttributeType.DECIMAL;
import static org.molgenis.data.meta.AttributeType.INT;
import static org.molgenis.data.meta.AttributeType.LONG;

import java.sql.BatchUpdateException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.sql.DataSource;
import org.molgenis.data.DuplicateValueException;
import org.molgenis.data.EntityTypeReferencedException;
import org.molgenis.data.ErrorCodedDataAccessException;
import org.molgenis.data.ExistingNullValueException;
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
import org.molgenis.data.transaction.TransactionExceptionTranslator;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.validation.ConstraintViolation;
import org.postgresql.util.PSQLException;
import org.postgresql.util.ServerErrorMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionException;

/** Translates PostgreSQL exceptions to MOLGENIS data exceptions */
@Component
class PostgreSqlExceptionTranslator extends SQLErrorCodeSQLExceptionTranslator
    implements TransactionExceptionTranslator {
  static final String VALUE_TOO_LONG_MSG = "One of the values being added is too long.";

  private static final Logger LOG = LoggerFactory.getLogger(PostgreSqlExceptionTranslator.class);
  private static final String ERROR_TRANSLATING_POSTGRES_EXC_MSG =
      "Error translating postgres exception: ";
  private static final String ERROR_TRANSLATING_EXCEPTION_MSG = "Error translating exception";
  private final EntityTypeRegistry entityTypeRegistry;

  PostgreSqlExceptionTranslator(DataSource dataSource, EntityTypeRegistry entityTypeRegistry) {
    super(requireNonNull(dataSource));
    this.entityTypeRegistry = requireNonNull(entityTypeRegistry);
  }

  @Override
  @Nullable
  @CheckForNull
  protected DataAccessException doTranslate(
      @Nonnull String task, @Nullable String sql, @Nonnull SQLException sqlException) {
    DataAccessException translatedException;

    DataAccessException dataAccessException = super.doTranslate(task, sql, sqlException);
    if (dataAccessException != null) {
      translatedException = doTranslate(dataAccessException, dataAccessException);
    } else {
      translatedException = doTranslate(sqlException, sqlException);
    }

    return translatedException;
  }

  @Override
  @Nullable
  @CheckForNull
  public DataAccessException doTranslate(TransactionException transactionException) {
    DataAccessException translatedException;

    Throwable cause = transactionException.getCause();
    if (cause instanceof PSQLException) {
      PSQLException psqlException = (PSQLException) cause;
      String task = "commit transaction";
      DataAccessException dataAccessException = super.doTranslate(task, null, psqlException);
      if (dataAccessException != null) {
        translatedException = doTranslate(transactionException, dataAccessException);
      } else {
        translatedException = doTranslate(transactionException, psqlException);
      }
    } else {
      translatedException = null;
    }

    return translatedException;
  }

  private DataAccessException doTranslate(
      Throwable sourceThrowable, DataAccessException dataAccessException) {
    DataAccessException translatedException;

    Throwable cause = dataAccessException.getCause();
    if (cause instanceof PSQLException) {
      translatedException = doTranslate(sourceThrowable, (PSQLException) cause);
    } else {
      translatedException = null;
    }

    return translatedException;
  }

  private DataAccessException doTranslate(Throwable sourceThrowable, SQLException sqlException) {
    SQLException relevantSqlException;
    if (sqlException instanceof BatchUpdateException) {
      relevantSqlException = sqlException.getNextException();
    } else {
      relevantSqlException = sqlException;
    }

    DataAccessException translatedException;
    if (relevantSqlException instanceof PSQLException) {
      translatedException = doTranslate(sourceThrowable, (PSQLException) relevantSqlException);
    } else {
      translatedException = null;
    }
    return translatedException;
  }

  private DataAccessException doTranslate(Throwable sourceThrowable, PSQLException pSqlException) {
    switch (pSqlException.getSQLState()) {
      case "22001":
        return translateValueTooLongViolation(sourceThrowable);
      case "22007": // invalid_datetime_format
      case "22P02": // not an integer exception
        return translateInvalidIntegerException(sourceThrowable, pSqlException);
      case "23502": // not_null_violation
        return translateNotNullViolation(sourceThrowable, pSqlException);
      case "23503": // foreign_key_violation
        return translateForeignKeyViolation(sourceThrowable, pSqlException);
      case "23505": // unique_violation
        return translateUniqueKeyViolation(sourceThrowable, pSqlException);
      case "23514": // check_violation
        return translateCheckConstraintViolation(sourceThrowable, pSqlException);
      case "2BP01":
        return translateDependentObjectsStillExist(sourceThrowable, pSqlException);
      case "42703":
        return translateUndefinedColumnException(pSqlException);
      case PostgreSqlQueryGenerator.ERR_CODE_READONLY_VIOLATION:
        return translateReadonlyViolation(sourceThrowable, pSqlException);
      default:
        return null;
    }
  }

  /** Package private for testability */
  ValueLengthExceededException translateValueTooLongViolation(Throwable sourceThrowable) {
    return new ValueLengthExceededException(sourceThrowable);
  }

  /** Package private for testability */
  ReadonlyValueException translateReadonlyViolation(
      Throwable sourceThrowable, PSQLException pSqlException) {
    Matcher matcher =
        Pattern.compile(
                "Updating read-only column \"?(.*?)\"? of table \"?(.*?)\"? with id \\[(.*?)] is not allowed")
            .matcher(pSqlException.getServerErrorMessage().getMessage());
    boolean matches = matcher.matches();
    if (!matches) {
      LOG.error(ERROR_TRANSLATING_POSTGRES_EXC_MSG, pSqlException);
      throw new RuntimeException(ERROR_TRANSLATING_EXCEPTION_MSG, pSqlException);
    }
    String colName = matcher.group(1);
    String tableName = matcher.group(2);
    String id = matcher.group(3);

    String attributeName = tryGetAttributeName(tableName, colName).orElse(null);
    String entityTypeId = tryGetEntityTypeName(tableName).orElse(null);
    return new ReadonlyValueException(entityTypeId, attributeName, id, sourceThrowable);
  }

  /** Package private for testability */
  EntityTypeReferencedException translateDependentObjectsStillExist(
      Throwable sourceThrowable, PSQLException pSqlException) {
    ServerErrorMessage serverErrorMessage = pSqlException.getServerErrorMessage();
    String detail = serverErrorMessage.getDetail();
    Matcher matcher =
        Pattern.compile(
                "constraint (.+) on table \"?([^\"]+)\"? depends on table \"?([^\"]+)\"?\n?")
            .matcher(detail);

    Map<String, Set<String>> entityTypeDependencyMap = new LinkedHashMap<>();
    while (matcher.find()) {
      String tableName = matcher.group(2);
      String dependentTableName = matcher.group(3);

      String entityTypeName = tryGetEntityTypeName(tableName).orElse(null);
      String dependentEntityTypeName = tryGetEntityTypeName(dependentTableName).orElse(null);
      Set<String> dependentTableNames =
          entityTypeDependencyMap.computeIfAbsent(
              dependentEntityTypeName, k -> new LinkedHashSet<>());
      dependentTableNames.add(entityTypeName);
    }

    if (entityTypeDependencyMap.isEmpty()) // no matches
    {
      LOG.error(ERROR_TRANSLATING_POSTGRES_EXC_MSG, pSqlException);
      throw new RuntimeException(ERROR_TRANSLATING_EXCEPTION_MSG, pSqlException);
    }

    return new EntityTypeReferencedException(entityTypeDependencyMap, sourceThrowable);
  }

  /** Package private for testability */
  static InvalidValueTypeException translateInvalidIntegerException(
      Throwable sourceThrowable, PSQLException pSqlException) {
    ServerErrorMessage serverErrorMessage = pSqlException.getServerErrorMessage();
    String message = serverErrorMessage.getMessage();
    Matcher matcher =
        Pattern.compile("invalid input syntax for \\b(?:type )?\\b(.+?): \"(.*?)\"")
            .matcher(message);
    boolean matches = matcher.matches();
    if (!matches) {
      throw new RuntimeException(ERROR_TRANSLATING_EXCEPTION_MSG, pSqlException);
    }
    String postgreSqlType = matcher.group(1);

    // convert PostgreSQL data type to attribute type:
    String type;
    switch (postgreSqlType) {
      case "boolean":
        type = BOOL.toString();
        break;
      case "date":
        type = DATE.toString();
        break;
      case "timestamp with time zone":
        type = DATE_TIME.toString();
        break;
      case "double precision":
        type = DECIMAL.toString();
        break;
      case "integer":
        type = INT.toString() + " or " + LONG.toString();
        break;
      default:
        type = postgreSqlType;
        break;
    }
    String value = matcher.group(2);

    return new InvalidValueTypeException(value, type, sourceThrowable);
  }

  /** Package private for testability */
  ErrorCodedDataAccessException translateNotNullViolation(
      Throwable sourceThrowable, PSQLException pSqlException) {
    ServerErrorMessage serverErrorMessage = pSqlException.getServerErrorMessage();
    String tableName = serverErrorMessage.getTable();
    String message = serverErrorMessage.getMessage();
    Matcher matcher =
        Pattern.compile("null value in column \"?(.*?)\"? violates not-null constraint")
            .matcher(message);
    boolean matches = matcher.matches();
    if (matches) {
      // exception message when adding data that does not match constraint
      String columnName = matcher.group(1);

      EntityTypeDescription entityTypeDescription =
          entityTypeRegistry.getEntityTypeDescription(tableName);
      entityTypeDescription.getAttributeDescriptionMap().get(columnName);

      String attributeName = tryGetAttributeName(tableName, columnName).orElse(null);
      String entityTypeId = tryGetEntityTypeName(tableName).orElse(null);

      return new ValueRequiredException(entityTypeId, attributeName, sourceThrowable);
    } else {
      // exception message when applying constraint on existing data
      matcher = Pattern.compile("column \"(.*?)\" contains null values").matcher(message);
      matches = matcher.matches();
      if (!matches) {
        throw new RuntimeException(ERROR_TRANSLATING_EXCEPTION_MSG, pSqlException);
      }
      String columnName = matcher.group(1);

      String attributeName = tryGetAttributeName(tableName, columnName).orElse(null);
      String entityTypeId = tryGetEntityTypeName(tableName).orElse(null);
      return new ExistingNullValueException(entityTypeId, attributeName, sourceThrowable);
    }
  }

  /** Package private for testability */
  ErrorCodedDataAccessException translateForeignKeyViolation(
      Throwable sourceThrowable, PSQLException pSqlException) {
    ServerErrorMessage serverErrorMessage = pSqlException.getServerErrorMessage();
    String tableName = serverErrorMessage.getTable();
    String detailMessage = serverErrorMessage.getDetail();
    Matcher m = Pattern.compile("\\((.*?)\\)").matcher(detailMessage);
    if (!m.find()) {
      LOG.error(ERROR_TRANSLATING_POSTGRES_EXC_MSG, pSqlException);
      throw new RuntimeException(ERROR_TRANSLATING_EXCEPTION_MSG, pSqlException);
    }
    String colName = m.group(1);
    if (!m.find()) {
      LOG.error(ERROR_TRANSLATING_POSTGRES_EXC_MSG, pSqlException);
      throw new RuntimeException(ERROR_TRANSLATING_EXCEPTION_MSG, pSqlException);
    }
    String value = m.group(1);

    String entityTypeId = tryGetEntityTypeName(tableName).orElse(null);

    if (detailMessage.contains("still referenced from")) {
      // ERROR: update or delete on table "x" violates foreign key constraint "y" on table "z"
      // Detail: Key (k)=(v) is still referenced from table "x".
      String refTableName = getRefTableFromForeignKeyPsqlException(pSqlException);
      String attrName = tryGetAttributeName(refTableName, colName).orElse(null);
      return new ValueReferencedException(entityTypeId, attrName, value, sourceThrowable);
    } else {
      // ERROR: insert or update on table "x" violates foreign key constraint "y"
      // Detail: Key (k)=(v) is not present in table "z".
      String attrName = tryGetAttributeName(tableName, colName).orElse(null);
      return new UnknownValueReferenceException(entityTypeId, attrName, value, sourceThrowable);
    }
  }

  private String getRefTableFromForeignKeyPsqlException(PSQLException pSqlException) {
    ServerErrorMessage serverErrorMessage = pSqlException.getServerErrorMessage();
    Matcher messageMatcher =
        Pattern.compile(
                "update or delete on table \"(.*)\" violates foreign key constraint \"(.*)\" on table \"(.*)\"")
            .matcher(serverErrorMessage.getMessage());
    if (!messageMatcher.matches()) {
      LOG.error(ERROR_TRANSLATING_POSTGRES_EXC_MSG, pSqlException);
      throw new RuntimeException(ERROR_TRANSLATING_EXCEPTION_MSG, pSqlException);
    }
    return messageMatcher.group(1);
  }

  /** Package private for testability */
  ErrorCodedDataAccessException translateUniqueKeyViolation(
      Throwable sourceThrowable, PSQLException pSqlException) {
    ServerErrorMessage serverErrorMessage = pSqlException.getServerErrorMessage();
    String tableName = serverErrorMessage.getTable();
    String detailMessage = serverErrorMessage.getDetail();
    Matcher matcher =
        Pattern.compile("Key \\(\"?(.*?)\"?\\)=\\((.*?)\\) already exists.").matcher(detailMessage);
    boolean matches = matcher.matches();
    if (matches) {
      // exception message when adding data that does not match constraint
      String[] columnNames = matcher.group(1).split(", ");
      if (columnNames.length == 1) {
        String columnName = columnNames[0];
        String value = matcher.group(2);

        String entityTypeId = tryGetEntityTypeName(tableName).orElse(null);
        String attributeName = tryGetAttributeName(tableName, columnName).orElse(null);
        return new ValueAlreadyExistsException(entityTypeId, attributeName, value, sourceThrowable);
      } else {
        String columnName = columnNames[columnNames.length - 1];
        String[] values = matcher.group(2).split(", ");
        String entityId = values[0];
        String value = values[1];

        String entityTypeId = tryGetEntityTypeName(tableName).orElse(null);
        String attributeName = tryGetAttributeName(tableName, columnName).orElse(null);
        return new ListValueAlreadyExistsException(
            entityTypeId, attributeName, entityId, value, sourceThrowable);
      }
    } else {
      // exception message when applying constraint on existing data
      matcher =
          Pattern.compile("Key \\(\"?(.*?)\"?\\)=\\((.*?)\\) is duplicated.")
              .matcher(detailMessage);
      matches = matcher.matches();
      if (matches) {
        String columnName = matcher.group(1);
        String value = matcher.group(2);

        String entityTypeId = tryGetEntityTypeName(tableName).orElse(null);
        String attributeName = tryGetAttributeName(tableName, columnName).orElse(null);
        return new DuplicateValueException(entityTypeId, attributeName, value, sourceThrowable);
      } else {
        LOG.error(ERROR_TRANSLATING_POSTGRES_EXC_MSG, pSqlException);
        throw new RuntimeException(ERROR_TRANSLATING_EXCEPTION_MSG, pSqlException);
      }
    }
  }

  /** Package private for testability */
  UnknownEnumValueException translateCheckConstraintViolation(
      Throwable sourceThrowable, PSQLException pSqlException) {
    ServerErrorMessage serverErrorMessage = pSqlException.getServerErrorMessage();
    String tableName = serverErrorMessage.getTable();
    String constraintName = serverErrorMessage.getConstraint();
    // constraint name: <tableName>_<columnName>_chk
    String columnName =
        constraintName.substring(tableName.length() + 1, constraintName.length() - 4);
    String entityTypeId = tryGetEntityTypeName(tableName).orElse(null);
    String attributeName = tryGetAttributeName(tableName, columnName).orElse(null);
    return new UnknownEnumValueException(entityTypeId, attributeName, sourceThrowable);
  }

  /** Package private for testability */
  static MolgenisValidationException translateUndefinedColumnException(
      PSQLException pSqlException) {
    ServerErrorMessage serverErrorMessage = pSqlException.getServerErrorMessage();
    String message = serverErrorMessage.getMessage(); // FIXME exposes internal message
    ConstraintViolation constraintViolation = new ConstraintViolation(message);
    return new MolgenisValidationException(singleton(constraintViolation));
  }

  /** Tries to determine the entity type identifier for this table name */
  private Optional<String> tryGetEntityTypeName(String tableName) {
    EntityTypeDescription entityTypeDescription =
        entityTypeRegistry.getEntityTypeDescription(tableName);
    String entityTypeId = entityTypeDescription != null ? entityTypeDescription.getId() : null;
    return Optional.ofNullable(entityTypeId);
  }

  /** Tries to determine the attribute name for this table column name */
  private Optional<String> tryGetAttributeName(String tableName, String colName) {
    String attributeName;

    EntityTypeDescription entityTypeDescription =
        entityTypeRegistry.getEntityTypeDescription(tableName);
    if (entityTypeDescription != null) {
      AttributeDescription attrDescription =
          entityTypeDescription.getAttributeDescriptionMap().get(colName);
      if (attrDescription != null) {
        attributeName = attrDescription.getName();
      } else {
        attributeName = null;
      }
    } else {
      attributeName = null;
    }

    return Optional.ofNullable(attributeName);
  }
}
