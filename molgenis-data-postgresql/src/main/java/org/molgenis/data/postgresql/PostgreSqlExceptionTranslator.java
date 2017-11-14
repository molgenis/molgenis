package org.molgenis.data.postgresql;

import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.postgresql.identifier.AttributeDescription;
import org.molgenis.data.postgresql.identifier.EntityTypeDescription;
import org.molgenis.data.postgresql.identifier.EntityTypeRegistry;
import org.molgenis.data.transaction.TransactionExceptionTranslator;
import org.molgenis.data.validation.*;
import org.postgresql.util.PSQLException;
import org.postgresql.util.ServerErrorMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionException;

import javax.sql.DataSource;
import java.sql.BatchUpdateException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.*;

/**
 * Translates PostgreSQL exceptions to MOLGENIS data exceptions
 */
@Component
class PostgreSqlExceptionTranslator extends SQLErrorCodeSQLExceptionTranslator implements TransactionExceptionTranslator
{
	private static final Logger LOG = LoggerFactory.getLogger(PostgreSqlExceptionTranslator.class);
	private final EntityTypeRegistry entityTypeRegistry;

	PostgreSqlExceptionTranslator(DataSource dataSource, EntityTypeRegistry entityTypeRegistry)
	{
		super(requireNonNull(dataSource));
		this.entityTypeRegistry = requireNonNull(entityTypeRegistry);
	}

	@Override
	protected DataAccessException doTranslate(String task, String sql, SQLException ex)
	{
		DataAccessException dataAccessException = super.doTranslate(task, sql, ex);
		if (dataAccessException == null)
		{
			return doTranslate(ex);
		}
		return doTranslate(dataAccessException);
	}

	private MolgenisDataAccessException doTranslate(DataAccessException dataAccessException)
	{
		Throwable cause = dataAccessException.getCause();
		if (!(cause instanceof PSQLException))
		{
			throw new RuntimeException(format("Unexpected exception class [%s]", cause.getClass().getSimpleName()));
		}

		PSQLException pSqlException = (PSQLException) cause;
		MolgenisDataAccessException molgenisDataException = doTranslate(pSqlException);
		if (molgenisDataException == null)
		{
			molgenisDataException = new MolgenisDataAccessException(dataAccessException);
		}
		return molgenisDataException;
	}

	private MolgenisDataAccessException doTranslate(SQLException sqlException)
	{
		if (sqlException instanceof BatchUpdateException)
		{
			sqlException = sqlException.getNextException();
		}
		if (!(sqlException instanceof PSQLException))
		{
			throw new RuntimeException(
					format("Unexpected exception class [%s]", sqlException.getClass().getSimpleName()));
		}

		PSQLException pSqlException = (PSQLException) sqlException;
		MolgenisDataAccessException molgenisDataException = doTranslate(pSqlException);
		if (molgenisDataException == null)
		{
			molgenisDataException = new MolgenisDataAccessException(sqlException);
		}
		return molgenisDataException;
	}

	private MolgenisDataAccessException doTranslate(PSQLException pSqlException)
	{
		switch (pSqlException.getSQLState())
		{
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
	 * @param pSqlException PostgreSQL exception
	 * @return translated validation exception
	 */
	MolgenisDataAccessException translateReadonlyViolation(PSQLException pSqlException)
	{
		Matcher matcher = Pattern.compile(
				"Updating read-only column \"?(.*?)\"? of table \"?(.*?)\"? with id \\[(.*?)] is not allowed")
								 .matcher(pSqlException.getServerErrorMessage().getMessage());
		boolean matches = matcher.matches();
		if (!matches)
		{
			LOG.error("Error translating postgres exception: ", pSqlException);
			throw new RuntimeException("Error translating exception", pSqlException);
		}
		String colName = matcher.group(1);
		String tableName = matcher.group(2);
		String id = matcher.group(3);

		String attributeName = getAttributeName(tableName, colName);
		String entityTypeName = getEntityTypeName(tableName);
		return new ReadOnlyConstraintViolationException(entityTypeName, attributeName, id, pSqlException);
	}

	/**
	 * Package private for testability
	 *
	 * @param pSqlException PostgreSQL exception
	 * @return translated validation exception
	 */
	MolgenisDataAccessException translateDependentObjectsStillExist(PSQLException pSqlException)
	{
		ServerErrorMessage serverErrorMessage = pSqlException.getServerErrorMessage();
		String detail = serverErrorMessage.getDetail();
		Matcher matcher = Pattern.compile("constraint (.+) on table \"?([^\"]+)\"? depends on table \"?([^\"]+)\"?\n?")
								 .matcher(detail);

		Map<String, Set<String>> entityTypeDependencyMap = new LinkedHashMap<>();
		while (matcher.find())
		{
			String tableName = matcher.group(2);
			String dependentTableName = matcher.group(3);

			String entityTypeName = getEntityTypeName(tableName);
			String dependentEntityTypeName = getEntityTypeName(dependentTableName);
			Set<String> dependentTableNames = entityTypeDependencyMap.computeIfAbsent(dependentEntityTypeName,
					k -> new LinkedHashSet<>());
			dependentTableNames.add(entityTypeName);
		}

		if (entityTypeDependencyMap.isEmpty()) // no matches
		{
			LOG.error("Error translating postgres exception: ", pSqlException);
			throw new RuntimeException("Error translating exception", pSqlException);
		}

		return new EntityTypeReferenceConstraintViolationException(entityTypeDependencyMap, pSqlException);
	}

	/**
	 * Package private for testability
	 *
	 * @param pSqlException PostgreSQL exception
	 * @return translated validation exception
	 */
	static MolgenisDataAccessException translateInvalidIntegerException(PSQLException pSqlException)
	{
		ServerErrorMessage serverErrorMessage = pSqlException.getServerErrorMessage();
		String message = serverErrorMessage.getMessage();
		Matcher matcher = Pattern.compile("invalid input syntax for \\b(?:type )?\\b(.+?): \"(.*?)\"").matcher(message);
		boolean matches = matcher.matches();
		if (!matches)
		{
			throw new RuntimeException("Error translating exception", pSqlException);
		}
		String postgreSqlType = matcher.group(1);

		// convert PostgreSQL data type to attribute type:
		String type;
		switch (postgreSqlType)
		{
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

		return new DataTypeConstraintViolationException(value, type, pSqlException);
	}

	/**
	 * Package private for testability
	 *
	 * @param pSqlException PostgreSQL exception
	 * @return translated validation exception
	 */
	MolgenisDataAccessException translateNotNullViolation(PSQLException pSqlException)
	{
		ServerErrorMessage serverErrorMessage = pSqlException.getServerErrorMessage();
		String tableName = serverErrorMessage.getTable();
		String message = serverErrorMessage.getMessage();
		Matcher matcher = Pattern.compile("null value in column \"?(.*?)\"? violates not-null constraint")
								 .matcher(message);
		boolean matches = matcher.matches();
		if (matches)
		{
			// exception message when adding data that does not match constraint
			String columnName = matcher.group(1);

			EntityTypeDescription entityTypeDescription = entityTypeRegistry.getEntityTypeDescription(tableName);
			entityTypeDescription.getAttributeDescriptionMap().get(columnName);

			String attributeName = getAttributeName(tableName, columnName);
			String entityTypeName = getEntityTypeName(tableName);

			return new NotNullConstraintViolationException(entityTypeName, attributeName, pSqlException);
		}
		else
		{
			// exception message when applying constraint on existing data
			matcher = Pattern.compile("column \"(.*?)\" contains null values").matcher(message);
			matches = matcher.matches();
			if (!matches)
			{
				throw new RuntimeException("Error translating exception", pSqlException);
			}
			String columnName = matcher.group(1);

			String attributeName = getAttributeName(tableName, columnName);
			String entityTypeName = getEntityTypeName(tableName);
			return new NotNullConstraintCreationException(entityTypeName, attributeName, pSqlException);
		}
	}

	/**
	 * Package private for testability
	 *
	 * @param pSqlException PostgreSQL exception
	 * @return translated validation exception
	 */
	MolgenisDataAccessException translateForeignKeyViolation(PSQLException pSqlException)
	{
		ServerErrorMessage serverErrorMessage = pSqlException.getServerErrorMessage();
		String tableName = serverErrorMessage.getTable();
		String detailMessage = serverErrorMessage.getDetail();
		Matcher m = Pattern.compile("\\((.*?)\\)").matcher(detailMessage);
		if (!m.find())
		{
			LOG.error("Error translating postgres exception: ", pSqlException);
			throw new RuntimeException("Error translating exception", pSqlException);
		}
		String colName = m.group(1);
		if (!m.find())
		{
			LOG.error("Error translating postgres exception: ", pSqlException);
			throw new RuntimeException("Error translating exception", pSqlException);
		}
		String value = m.group(1);

		String entityTypeName = getEntityTypeName(tableName);

		String attrName;
		if (detailMessage.contains("still referenced from"))
		{
			// ERROR: update or delete on table "x" violates foreign key constraint "y" on table "z"
			// Detail: Key (k)=(v) is still referenced from table "x".

			String refTableName = getRefTableFromForeignKeyPsqlException(pSqlException);
			attrName = getAttributeName(refTableName, colName);
			return new EntityReferenceConstraintViolationException(entityTypeName, attrName, value, pSqlException);
		}
		else
		{
			// ERROR: insert or update on table "x" violates foreign key constraint "y"
			// Detail: Key (k)=(v) is not present in table "z".

			attrName = getAttributeName(tableName, colName);
			return new EntityReferenceUnknownConstraintViolationException(entityTypeName, attrName, value,
					pSqlException);
		}
	}

	private String getRefTableFromForeignKeyPsqlException(PSQLException pSqlException)
	{
		ServerErrorMessage serverErrorMessage = pSqlException.getServerErrorMessage();
		Matcher messageMatcher = Pattern.compile(
				"update or delete on table \"(.*)\" violates foreign key constraint \"(.*)\" on table \"(.*)\"")
										.matcher(serverErrorMessage.getMessage());
		if (!messageMatcher.matches())
		{
			LOG.error("Error translating postgres exception: ", pSqlException);
			throw new RuntimeException("Error translating exception", pSqlException);
		}
		return messageMatcher.group(1);
	}

	/**
	 * Package private for testability
	 *
	 * @param pSqlException PostgreSQL exception
	 * @return translated validation exception
	 */
	MolgenisDataAccessException translateUniqueKeyViolation(PSQLException pSqlException)
	{
		ServerErrorMessage serverErrorMessage = pSqlException.getServerErrorMessage();
		String tableName = serverErrorMessage.getTable();
		String detailMessage = serverErrorMessage.getDetail();
		Matcher matcher = Pattern.compile("Key \\(\"?(.*?)\"?\\)=\\((.*?)\\) already exists.").matcher(detailMessage);
		boolean matches = matcher.matches();
		if (matches)
		{
			ConstraintViolation constraintViolation;

			// exception message when adding data that does not match constraint
			String[] columnNames = matcher.group(1).split(", ");
			if (columnNames.length == 1)
			{
				String columnName = columnNames[0];
				String value = matcher.group(2);

				final String entityTypeName = getEntityTypeName(tableName);
				final String attributeName = getAttributeName(tableName, columnName);

				return new UniqueConstraintViolationException(entityTypeName, attributeName, value, pSqlException);
			}
			else
			{
				String columnName = columnNames[columnNames.length - 1];
				String[] values = matcher.group(2).split(", ");
				String idValue = values[0];
				String value = values[1];

				final String attributeName = getAttributeName(tableName, columnName);
				final String entityTypeName = getEntityTypeName(tableName);
				return new UniqueReferenceConstraintViolationException(entityTypeName, attributeName, value, idValue,
						pSqlException);
			}
		}
		else
		{
			// exception message when applying constraint on existing data
			matcher = Pattern.compile("Key \\(\"?(.*?)\"?\\)=\\((.*?)\\) is duplicated.").matcher(detailMessage);
			matches = matcher.matches();
			if (matches)
			{
				String columnName = matcher.group(1);
				String value = matcher.group(2);

				final String attributeName = getAttributeName(tableName, columnName);
				final String entityTypeName = getEntityTypeName(tableName);
				return new UniqueConstraintCreationException(entityTypeName, attributeName, value, pSqlException);
			}
			else
			{
				LOG.error("Error translating postgres exception: ", pSqlException);
				throw new RuntimeException("Error translating exception", pSqlException);
			}
		}
	}

	/**
	 * Package private for testability
	 *
	 * @param pSqlException PostgreSQL exception
	 * @return translated validation exception
	 */
	MolgenisDataAccessException translateCheckConstraintViolation(PSQLException pSqlException)
	{
		ServerErrorMessage serverErrorMessage = pSqlException.getServerErrorMessage();
		String tableName = serverErrorMessage.getTable();
		String entityTypeName = getEntityTypeName(tableName);
		return new EnumConstraintModificationException(entityTypeName, pSqlException);
	}

	/**
	 * Package private for testability
	 *
	 * @param pSqlException PostgreSQL exception
	 * @return translated validation exception
	 */
	static MolgenisDataAccessException translateUndefinedColumnException(PSQLException pSqlException)
	{
		// PSQL exception contains column name, but not the table name so we can't determine the attribute name
		throw new RuntimeException("Error translating exception", pSqlException);
	}

	@Override
	public MolgenisDataAccessException doTranslate(TransactionException transactionException)
	{
		Throwable cause = transactionException.getCause();
		if (!(cause instanceof PSQLException))
		{
			return null;
		}

		PSQLException pSqlException = (PSQLException) cause;
		return doTranslate(pSqlException);
	}

	/**
	 * Returns the entity type fully qualified name for this table name
	 *
	 * @param tableName table name
	 * @return entity type fully qualified name
	 */
	private String getEntityTypeName(String tableName)
	{
		EntityTypeDescription entityTypeDescription = entityTypeRegistry.getEntityTypeDescription(tableName);
		if (entityTypeDescription == null)
		{
			throw new RuntimeException(format("Unknown entity for table name [%s]", tableName));
		}
		return entityTypeDescription.getId();
	}

	/**
	 * Returns the attribute name for this table name
	 *
	 * @param tableName table name
	 * @param colName   column name
	 * @return attribute name
	 */
	private String getAttributeName(String tableName, String colName)
	{
		EntityTypeDescription entityTypeDescription = entityTypeRegistry.getEntityTypeDescription(tableName);
		if (entityTypeDescription == null)
		{
			throw new RuntimeException(format("Unknown entity for table name [%s]", tableName));
		}
		AttributeDescription attrDescription = entityTypeDescription.getAttributeDescriptionMap().get(colName);
		if (attrDescription == null)
		{
			throw new RuntimeException(format("Unknown attribute for column name [%s]", colName));
		}
		return attrDescription.getName();
	}
}
