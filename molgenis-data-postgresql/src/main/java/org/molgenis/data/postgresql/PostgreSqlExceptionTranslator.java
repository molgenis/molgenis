package org.molgenis.data.postgresql;

import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.validation.ConstraintViolation;
import org.molgenis.data.validation.MolgenisValidationException;
import org.postgresql.util.PSQLException;
import org.postgresql.util.ServerErrorMessage;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;

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
import static org.molgenis.AttributeType.*;

/**
 * Translates PostgreSQL exceptions to MOLGENIS data exceptions
 */
class PostgreSqlExceptionTranslator extends SQLErrorCodeSQLExceptionTranslator
{
	PostgreSqlExceptionTranslator(DataSource dataSource)
	{
		super(requireNonNull(dataSource));
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

	private static MolgenisDataException doTranslate(DataAccessException dataAccessException)
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

	private static MolgenisDataException doTranslate(SQLException sqlException)
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

	private static MolgenisDataException doTranslate(PSQLException pSqlException)
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
	static MolgenisValidationException translateDependentObjectsStillExist(PSQLException pSqlException)
	{
		ServerErrorMessage serverErrorMessage = pSqlException.getServerErrorMessage();
		String detail = serverErrorMessage.getDetail();
		Matcher matcher = Pattern.compile("constraint (.*?) on table \"(.*?)\" depends on table \"(.*?)\"")
				.matcher(detail);

		String table = null;
		Set<String> dependentTables = new LinkedHashSet<>();
		while (matcher.find())
		{
			table = matcher.group(2);
			dependentTables.add(matcher.group(3));
		}

		if (table == null) // no matches
		{
			throw new RuntimeException("Error translating exception", pSqlException);
		}

		String message;
		if (dependentTables.size() == 1)
		{
			message = format("Cannot delete entity '%s' because entity '%s' depends on it.", table,
					dependentTables.iterator().next());
		}
		else
		{
			message = format("Cannot delete entity '%s' because entities '%s' depend on it.", table,
					dependentTables.stream().collect(joining(", ")));
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
	static MolgenisValidationException translateNotNullViolation(PSQLException pSqlException)
	{
		ServerErrorMessage serverErrorMessage = pSqlException.getServerErrorMessage();
		String tableName = serverErrorMessage.getTable();
		String message = serverErrorMessage.getMessage();
		Matcher matcher = Pattern.compile("null value in column \"(.*?)\" violates not-null constraint")
				.matcher(message);
		boolean matches = matcher.matches();
		if (matches)
		{
			// exception message when adding data that does not match constraint
			String columnName = matcher.group(1);

			ConstraintViolation constraintViolation = new ConstraintViolation(
					format("The attribute '%s' of entity '%s' can not be null.", columnName, tableName), null);
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
					format("The attribute '%s' of entity '%s' contains null values.", columnName, tableName), null);
			return new MolgenisValidationException(singleton(constraintViolation));
		}
	}

	/**
	 * Package private for testability
	 *
	 * @param pSqlException PostgreSQL exception
	 * @return translated validation exception
	 */
	static MolgenisValidationException translateForeignKeyViolation(PSQLException pSqlException)
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
				format(constraintViolationMessageTemplate, value, colName, tableName), null);
		return new MolgenisValidationException(singleton(constraintViolation));
	}

	/**
	 * Package private for testability
	 *
	 * @param pSqlException PostgreSQL exception
	 * @return translated validation exception
	 */
	static MolgenisValidationException translateUniqueKeyViolation(PSQLException pSqlException)
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
					format("Duplicate value '%s' for unique attribute '%s' from entity '%s'.", value, columnName,
							tableName), null);
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
						format("The attribute '%s' of entity '%s' contains duplicate value '%s'.", columnName,
								tableName, value), null);
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
	static MolgenisValidationException translateCheckConstraintViolation(PSQLException pSqlException)
	{
		ServerErrorMessage serverErrorMessage = pSqlException.getServerErrorMessage();
		String tableName = serverErrorMessage.getTable();
		String constraintName = serverErrorMessage.getConstraint();
		// constraint name: <tableName>_<columnName>_chk
		String columnName = constraintName.substring(tableName.length() + 1, constraintName.length() - 4);
		ConstraintViolation constraintViolation = new ConstraintViolation(
				format("Unknown enum value for attribute '%s' of entity '%s'.", columnName, tableName), null);
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
		String message = serverErrorMessage.getMessage();
		ConstraintViolation constraintViolation = new ConstraintViolation(message);
		return new MolgenisValidationException(singleton(constraintViolation));
	}
}
