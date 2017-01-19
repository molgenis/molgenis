package org.molgenis.data.postgresql;

import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.postgresql.identifier.AttributeDescription;
import org.molgenis.data.postgresql.identifier.EntityTypeDescription;
import org.molgenis.data.postgresql.identifier.EntityTypeRegistry;
import org.molgenis.data.transaction.TransactionExceptionTranslator;
import org.molgenis.data.validation.ConstraintViolation;
import org.molgenis.data.validation.MolgenisValidationException;
import org.postgresql.util.PSQLException;
import org.postgresql.util.ServerErrorMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionException;

import javax.sql.DataSource;
import java.sql.BatchUpdateException;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.util.Collections.singleton;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static org.molgenis.data.meta.AttributeType.*;

/**
 * Translates PostgreSQL exceptions to MOLGENIS data exceptions
 */
@Component
class PostgreSqlExceptionTranslator extends SQLErrorCodeSQLExceptionTranslator implements TransactionExceptionTranslator
{
	private final EntityTypeRegistry entityTypeRegistry;

	@Autowired
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

	private MolgenisDataException doTranslate(DataAccessException dataAccessException)
	{
		Throwable cause = dataAccessException.getCause();
		if (!(cause instanceof PSQLException))
		{
			throw new RuntimeException(format("Unexpected exception class [%s]", cause.getClass().getSimpleName()));
		}

		PSQLException pSqlException = (PSQLException) cause;
		MolgenisDataException molgenisDataException = doTranslate(pSqlException);
		if (molgenisDataException == null)
		{
			molgenisDataException = new MolgenisDataException(dataAccessException);
		}
		return molgenisDataException;
	}

	private MolgenisDataException doTranslate(SQLException sqlException)
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
		MolgenisDataException molgenisDataException = doTranslate(pSqlException);
		if (molgenisDataException == null)
		{
			molgenisDataException = new MolgenisDataException(sqlException);
		}
		return molgenisDataException;
	}

	private MolgenisDataException doTranslate(PSQLException pSqlException)
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
	MolgenisValidationException translateReadonlyViolation(PSQLException pSqlException)
	{
		Matcher matcher = Pattern
				.compile("Updating read-only column \"?(.*?)\"? of table \"?(.*?)\"? with id \\[(.*?)] is not allowed")
				.matcher(pSqlException.getServerErrorMessage().getMessage());
		boolean matches = matcher.matches();
		if (!matches)
		{
			throw new RuntimeException("Error translating exception", pSqlException);
		}
		String colName = matcher.group(1);
		String tableName = matcher.group(2);
		String id = matcher.group(3);
		ConstraintViolation constraintViolation = new ConstraintViolation(
				format("Updating read-only attribute '%s' of entity '%s' with id '%s' is not allowed.",
						getAttributeName(tableName, colName), getEntityTypeName(tableName), id));
		return new MolgenisValidationException(singleton(constraintViolation));
	}

	/**
	 * Package private for testability
	 *
	 * @param pSqlException PostgreSQL exception
	 * @return translated validation exception
	 */
	MolgenisValidationException translateDependentObjectsStillExist(PSQLException pSqlException)
	{
		ServerErrorMessage serverErrorMessage = pSqlException.getServerErrorMessage();
		String detail = serverErrorMessage.getDetail();
		Matcher matcher = Pattern.compile("constraint (.+) on table \"?([^\"]+)\"? depends on table \"?([^\"]+)\"?\n?")
				.matcher(detail);

		String tableName = null;
		Set<String> dependentTables = new LinkedHashSet<>();
		while (matcher.find())
		{
			tableName = matcher.group(2);
			dependentTables.add(matcher.group(3));
		}

		if (tableName == null) // no matches
		{
			throw new RuntimeException("Error translating exception", pSqlException);
		}

		String message;
		if (dependentTables.size() == 1)
		{
			message = format("Cannot delete entity '%s' because entity '%s' depends on it.",
					getEntityTypeName(tableName), getEntityTypeName(dependentTables.iterator().next()));
		}
		else
		{
			message = format("Cannot delete entity '%s' because entities '%s' depend on it.",
					getEntityTypeName(tableName),
					dependentTables.stream().map(this::getEntityTypeName).collect(joining(", ")));
		}
		ConstraintViolation constraintViolation = new ConstraintViolation(message, null);
		return new MolgenisValidationException(singleton(constraintViolation));
	}

	/**
	 * Package private for testability
	 *
	 * @param pSqlException PostgreSQL exception
	 * @return translated validation exception
	 */
	static MolgenisValidationException translateInvalidIntegerException(PSQLException pSqlException)
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
			case "timestamp":
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

		ConstraintViolation constraintViolation = new ConstraintViolation(
				format("Value [%s] of this entity attribute is not of type [%s].", value, type), null);
		return new MolgenisValidationException(singleton(constraintViolation));
	}

	/**
	 * Package private for testability
	 *
	 * @param pSqlException PostgreSQL exception
	 * @return translated validation exception
	 */
	MolgenisValidationException translateNotNullViolation(PSQLException pSqlException)
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

			ConstraintViolation constraintViolation = new ConstraintViolation(
					format("The attribute '%s' of entity '%s' can not be null.",
							getAttributeName(tableName, columnName), getEntityTypeName(tableName)), null);
			return new MolgenisValidationException(singleton(constraintViolation));
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

			ConstraintViolation constraintViolation = new ConstraintViolation(
					format("The attribute '%s' of entity '%s' contains null values.",
							getAttributeName(tableName, columnName), getEntityTypeName(tableName)), null);
			return new MolgenisValidationException(singleton(constraintViolation));
		}
	}

	/**
	 * Package private for testability
	 *
	 * @param pSqlException PostgreSQL exception
	 * @return translated validation exception
	 */
	MolgenisValidationException translateForeignKeyViolation(PSQLException pSqlException)
	{
		ServerErrorMessage serverErrorMessage = pSqlException.getServerErrorMessage();
		String tableName = serverErrorMessage.getTable();
		String detailMessage = serverErrorMessage.getDetail();
		Matcher m = Pattern.compile("\\((.*?)\\)").matcher(detailMessage);
		if (!m.find())
		{
			throw new RuntimeException("Error translating exception", pSqlException);
		}
		String colName = m.group(1);
		if (!m.find())
		{
			throw new RuntimeException("Error translating exception", pSqlException);
		}
		String value = m.group(1);

		String constraintViolationMessageTemplate;
		if (detailMessage.contains("still referenced from"))
		{
			// ERROR: update or delete on table "x" violates foreign key constraint "y" on table "z"
			// Detail: Key (k)=(v) is still referenced from table "x".
			constraintViolationMessageTemplate = "Value '%s' for attribute '%s' is referenced by entity '%s'.";
		}
		else
		{
			// ERROR: insert or update on table "x" violates foreign key constraint "y"
			// Detail: Key (k)=(v) is not present in table "z".
			constraintViolationMessageTemplate = "Unknown xref value '%s' for attribute '%s' of entity '%s'.";
		}
		ConstraintViolation constraintViolation = new ConstraintViolation(
				format(constraintViolationMessageTemplate, value, getAttributeName(tableName, colName),
						getEntityTypeName(tableName)), null);
		return new MolgenisValidationException(singleton(constraintViolation));
	}

	/**
	 * Package private for testability
	 *
	 * @param pSqlException PostgreSQL exception
	 * @return translated validation exception
	 */
	MolgenisValidationException translateUniqueKeyViolation(PSQLException pSqlException)
	{
		ServerErrorMessage serverErrorMessage = pSqlException.getServerErrorMessage();
		String tableName = serverErrorMessage.getTable();
		String detailMessage = serverErrorMessage.getDetail();
		Matcher matcher = Pattern.compile("Key \\((.*?)\\)=\\((.*?)\\) already exists.").matcher(detailMessage);
		boolean matches = matcher.matches();
		if (matches)
		{
			// exception message when adding data that does not match constraint
			String columnName = matcher.group(1);
			String value = matcher.group(2);

			ConstraintViolation constraintViolation = new ConstraintViolation(
					format("Duplicate value '%s' for unique attribute '%s' from entity '%s'.", value,
							getAttributeName(tableName, columnName), getEntityTypeName(tableName)), null);
			return new MolgenisValidationException(singleton(constraintViolation));
		}
		else
		{
			// exception message when applying constraint on existing data
			matcher = Pattern.compile("Key \\((.*?)\\)=\\((.*?)\\) is duplicated.").matcher(detailMessage);
			matches = matcher.matches();
			if (matches)
			{
				String columnName = matcher.group(1);
				String value = matcher.group(2);

				ConstraintViolation constraintViolation = new ConstraintViolation(
						format("The attribute '%s' of entity '%s' contains duplicate value '%s'.",
								getAttributeName(tableName, columnName), getEntityTypeName(tableName), value), null);
				return new MolgenisValidationException(singleton(constraintViolation));
			}
			else
			{
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
	MolgenisValidationException translateCheckConstraintViolation(PSQLException pSqlException)
	{
		ServerErrorMessage serverErrorMessage = pSqlException.getServerErrorMessage();
		String tableName = serverErrorMessage.getTable();
		String constraintName = serverErrorMessage.getConstraint();
		// constraint name: <tableName>_<columnName>_chk
		String columnName = constraintName.substring(tableName.length() + 1, constraintName.length() - 4);
		ConstraintViolation constraintViolation = new ConstraintViolation(
				format("Unknown enum value for attribute '%s' of entity '%s'.", getAttributeName(tableName, columnName),
						getEntityTypeName(tableName)), null);
		return new MolgenisValidationException(singleton(constraintViolation));
	}

	/**
	 * Package private for testability
	 *
	 * @param pSqlException PostgreSQL exception
	 * @return translated validation exception
	 */
	static MolgenisValidationException translateUndefinedColumnException(PSQLException pSqlException)
	{
		ServerErrorMessage serverErrorMessage = pSqlException.getServerErrorMessage();
		String message = serverErrorMessage.getMessage(); // FIXME exposes internal message
		ConstraintViolation constraintViolation = new ConstraintViolation(message);
		return new MolgenisValidationException(singleton(constraintViolation));
	}

	@Override
	public MolgenisDataException doTranslate(TransactionException transactionException)
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
		return entityTypeDescription.getFullyQualifiedName();
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
