package org.molgenis.data.postgresql;

import static java.lang.String.format;
import static java.util.Collections.singleton;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toCollection;
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
import javax.sql.DataSource;
import org.molgenis.data.MolgenisDataException;
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
  private static final String TOKEN_UNKNOWN = "<unknown>";

  PostgreSqlExceptionTranslator(DataSource dataSource, EntityTypeRegistry entityTypeRegistry) {
    super(requireNonNull(dataSource));
    this.entityTypeRegistry = requireNonNull(entityTypeRegistry);
  }

  @Override
  protected DataAccessException doTranslate(String task, String sql, SQLException ex) {
    DataAccessException dataAccessException = super.doTranslate(task, sql, ex);
    if (dataAccessException == null) {
      return doTranslate(ex);
    }
    return doTranslate(dataAccessException);
  }

  private MolgenisDataException doTranslate(DataAccessException dataAccessException) {
    Throwable cause = dataAccessException.getCause();
    if (!(cause instanceof PSQLException)) {
      throw new RuntimeException(
          format("Unexpected exception class [%s]", cause.getClass().getSimpleName()));
    }

    PSQLException pSqlException = (PSQLException) cause;
    MolgenisDataException molgenisDataException = doTranslate(pSqlException);
    if (molgenisDataException == null) {
      molgenisDataException = new MolgenisDataException(dataAccessException);
    }
    return molgenisDataException;
  }

  private MolgenisDataException doTranslate(SQLException sqlException) {
    if (sqlException instanceof BatchUpdateException) {
      sqlException = sqlException.getNextException();
    }
    if (!(sqlException instanceof PSQLException)) {
      throw new RuntimeException(
          format("Unexpected exception class [%s]", sqlException.getClass().getSimpleName()));
    }

    PSQLException pSqlException = (PSQLException) sqlException;
    MolgenisDataException molgenisDataException = doTranslate(pSqlException);
    if (molgenisDataException == null) {
      molgenisDataException = new MolgenisDataException(sqlException);
    }
    return molgenisDataException;
  }

  private MolgenisDataException doTranslate(PSQLException pSqlException) {
    switch (pSqlException.getSQLState()) {
      case "22001":
        return translateValueTooLongViolation();
      case "22007": // invalid_datetime_format
      case "22P02": // not an integer exception
        return translateInvalidIntegerException(pSqlException);
      case "23502": // not_null_violation
        return translateNotNullViolation(pSqlException);
      case "23503": // foreign_key_violation
        return translateForeignKeyViolation(pSqlException);
      case "23505": // unique_violation
        return translateUniqueKeyViolation(pSqlException);
      case "23514": // check_violation
        return translateCheckConstraintViolation(pSqlException);
      case "2BP01":
        return translateDependentObjectsStillExist(pSqlException);
      case "42703":
        return translateUndefinedColumnException(pSqlException);
      case PostgreSqlQueryGenerator.ERR_CODE_READONLY_VIOLATION:
        return translateReadonlyViolation(pSqlException);
      default:
        return null;
    }
  }

  /**
   * Package private for testability
   *
   * @return translated validation exception
   */
  MolgenisValidationException translateValueTooLongViolation() {
    ConstraintViolation constraintViolation = new ConstraintViolation(VALUE_TOO_LONG_MSG);
    return new MolgenisValidationException(singleton(constraintViolation));
  }

  /**
   * Package private for testability
   *
   * @param pSqlException PostgreSQL exception
   * @return translated validation exception
   */
  MolgenisValidationException translateReadonlyViolation(PSQLException pSqlException) {
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
    ConstraintViolation constraintViolation =
        new ConstraintViolation(
            format(
                "Updating read-only attribute '%s' of entity '%s' with id '%s' is not allowed.",
                tryGetAttributeName(tableName, colName).orElse(TOKEN_UNKNOWN),
                tryGetEntityTypeName(tableName).orElse(TOKEN_UNKNOWN),
                id));
    return new MolgenisValidationException(singleton(constraintViolation));
  }

  /**
   * Package private for testability
   *
   * @param pSqlException PostgreSQL exception
   * @return translated validation exception
   */
  MolgenisValidationException translateDependentObjectsStillExist(PSQLException pSqlException) {
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

      String entityTypeName = tryGetEntityTypeName(tableName).orElse(TOKEN_UNKNOWN);
      String dependentEntityTypeName =
          tryGetEntityTypeName(dependentTableName).orElse(TOKEN_UNKNOWN);
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

    Set<ConstraintViolation> constraintViolations =
        entityTypeDependencyMap
            .entrySet()
            .stream()
            .map(
                entry -> {
                  String message;
                  if (entry.getValue().size() == 1) {
                    message =
                        format(
                            "Cannot delete entity '%s' because entity '%s' depends on it.",
                            entry.getKey(), entry.getValue().iterator().next());
                  } else {
                    message =
                        format(
                            "Cannot delete entity '%s' because entities '%s' depend on it.",
                            entry.getKey(), entry.getValue().stream().collect(joining(", ")));
                  }
                  return new ConstraintViolation(message, null);
                })
            .collect(toCollection(LinkedHashSet::new));

    return new MolgenisValidationException(constraintViolations);
  }

  /**
   * Package private for testability
   *
   * @param pSqlException PostgreSQL exception
   * @return translated validation exception
   */
  static MolgenisValidationException translateInvalidIntegerException(PSQLException pSqlException) {
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

    ConstraintViolation constraintViolation =
        new ConstraintViolation(
            format("Value [%s] of this entity attribute is not of type [%s].", value, type), null);
    return new MolgenisValidationException(singleton(constraintViolation));
  }

  /**
   * Package private for testability
   *
   * @param pSqlException PostgreSQL exception
   * @return translated validation exception
   */
  MolgenisValidationException translateNotNullViolation(PSQLException pSqlException) {
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

      ConstraintViolation constraintViolation =
          new ConstraintViolation(
              format(
                  "The attribute '%s' of entity '%s' can not be null.",
                  tryGetAttributeName(tableName, columnName).orElse(TOKEN_UNKNOWN),
                  tryGetEntityTypeName(tableName).orElse(TOKEN_UNKNOWN)),
              null);
      return new MolgenisValidationException(singleton(constraintViolation));
    } else {
      // exception message when applying constraint on existing data
      matcher = Pattern.compile("column \"(.*?)\" contains null values").matcher(message);
      matches = matcher.matches();
      if (!matches) {
        throw new RuntimeException(ERROR_TRANSLATING_EXCEPTION_MSG, pSqlException);
      }
      String columnName = matcher.group(1);

      ConstraintViolation constraintViolation =
          new ConstraintViolation(
              format(
                  "The attribute '%s' of entity '%s' contains null values.",
                  tryGetAttributeName(tableName, columnName),
                  tryGetEntityTypeName(tableName).orElse(TOKEN_UNKNOWN)),
              null);
      return new MolgenisValidationException(singleton(constraintViolation));
    }
  }

  /**
   * Package private for testability
   *
   * @param pSqlException PostgreSQL exception
   * @return translated validation exception
   */
  MolgenisValidationException translateForeignKeyViolation(PSQLException pSqlException) {
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

    String constraintViolationMessageTemplate;
    String attrName;
    if (detailMessage.contains("still referenced from")) {
      // ERROR: update or delete on table "x" violates foreign key constraint "y" on table "z"
      // Detail: Key (k)=(v) is still referenced from table "x".
      constraintViolationMessageTemplate =
          "Value '%s' for attribute '%s' is referenced by entity '%s'.";
      String refTableName = getRefTableFromForeignKeyPsqlException(pSqlException);
      attrName = tryGetAttributeName(refTableName, colName).orElse(TOKEN_UNKNOWN);
    } else {
      // ERROR: insert or update on table "x" violates foreign key constraint "y"
      // Detail: Key (k)=(v) is not present in table "z".
      constraintViolationMessageTemplate =
          "Unknown xref value '%s' for attribute '%s' of entity '%s'.";
      attrName = tryGetAttributeName(tableName, colName).orElse(TOKEN_UNKNOWN);
    }
    ConstraintViolation constraintViolation =
        new ConstraintViolation(
            format(
                constraintViolationMessageTemplate,
                value,
                attrName,
                tryGetEntityTypeName(tableName).orElse(TOKEN_UNKNOWN)),
            null);
    return new MolgenisValidationException(singleton(constraintViolation));
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

  /**
   * Package private for testability
   *
   * @param pSqlException PostgreSQL exception
   * @return translated validation exception
   */
  MolgenisValidationException translateUniqueKeyViolation(PSQLException pSqlException) {
    ServerErrorMessage serverErrorMessage = pSqlException.getServerErrorMessage();
    String tableName = serverErrorMessage.getTable();
    String detailMessage = serverErrorMessage.getDetail();
    Matcher matcher =
        Pattern.compile("Key \\(\"?(.*?)\"?\\)=\\((.*?)\\) already exists.").matcher(detailMessage);
    boolean matches = matcher.matches();
    if (matches) {
      ConstraintViolation constraintViolation;

      // exception message when adding data that does not match constraint
      String[] columnNames = matcher.group(1).split(", ");
      if (columnNames.length == 1) {
        String columnName = columnNames[0];
        String value = matcher.group(2);

        constraintViolation =
            new ConstraintViolation(
                format(
                    "Duplicate value '%s' for unique attribute '%s' from entity '%s'.",
                    value,
                    tryGetAttributeName(tableName, columnName).orElse(TOKEN_UNKNOWN),
                    tryGetEntityTypeName(tableName).orElse(TOKEN_UNKNOWN)),
                null);
      } else {
        String columnName = columnNames[columnNames.length - 1];
        String[] values = matcher.group(2).split(", ");
        String idValue = values[0];
        String value = values[1];

        constraintViolation =
            new ConstraintViolation(
                format(
                    "Duplicate list value '%s' for attribute '%s' from entity '%s' with id '%s'.",
                    value,
                    tryGetAttributeName(tableName, columnName).orElse(TOKEN_UNKNOWN),
                    tryGetEntityTypeName(tableName).orElse(TOKEN_UNKNOWN),
                    idValue),
                null);
      }
      return new MolgenisValidationException(singleton(constraintViolation));
    } else {
      // exception message when applying constraint on existing data
      matcher =
          Pattern.compile("Key \\(\"?(.*?)\"?\\)=\\((.*?)\\) is duplicated.")
              .matcher(detailMessage);
      matches = matcher.matches();
      if (matches) {
        String columnName = matcher.group(1);
        String value = matcher.group(2);

        ConstraintViolation constraintViolation =
            new ConstraintViolation(
                format(
                    "The attribute '%s' of entity '%s' contains duplicate value '%s'.",
                    tryGetAttributeName(tableName, columnName).orElse(TOKEN_UNKNOWN),
                    tryGetEntityTypeName(tableName).orElse(TOKEN_UNKNOWN),
                    value),
                null);
        return new MolgenisValidationException(singleton(constraintViolation));
      } else {
        LOG.error(ERROR_TRANSLATING_POSTGRES_EXC_MSG, pSqlException);
        throw new RuntimeException(ERROR_TRANSLATING_EXCEPTION_MSG, pSqlException);
      }
    }
  }

  /**
   * Package private for testability
   *
   * @param pSqlException PostgreSQL exception
   * @return translated validation exception
   */
  MolgenisValidationException translateCheckConstraintViolation(PSQLException pSqlException) {
    ServerErrorMessage serverErrorMessage = pSqlException.getServerErrorMessage();
    String tableName = serverErrorMessage.getTable();
    String constraintName = serverErrorMessage.getConstraint();
    // constraint name: <tableName>_<columnName>_chk
    String columnName =
        constraintName.substring(tableName.length() + 1, constraintName.length() - 4);
    ConstraintViolation constraintViolation =
        new ConstraintViolation(
            format(
                "Unknown enum value for attribute '%s' of entity '%s'.",
                tryGetAttributeName(tableName, columnName).orElse(TOKEN_UNKNOWN),
                tryGetEntityTypeName(tableName).orElse(TOKEN_UNKNOWN)),
            null);
    return new MolgenisValidationException(singleton(constraintViolation));
  }

  /**
   * Package private for testability
   *
   * @param pSqlException PostgreSQL exception
   * @return translated validation exception
   */
  static MolgenisValidationException translateUndefinedColumnException(
      PSQLException pSqlException) {
    ServerErrorMessage serverErrorMessage = pSqlException.getServerErrorMessage();
    String message = serverErrorMessage.getMessage(); // FIXME exposes internal message
    ConstraintViolation constraintViolation = new ConstraintViolation(message);
    return new MolgenisValidationException(singleton(constraintViolation));
  }

  @Override
  public MolgenisDataException doTranslate(TransactionException transactionException) {
    Throwable cause = transactionException.getCause();
    if (!(cause instanceof PSQLException)) {
      return null;
    }

    PSQLException pSqlException = (PSQLException) cause;
    return doTranslate(pSqlException);
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
