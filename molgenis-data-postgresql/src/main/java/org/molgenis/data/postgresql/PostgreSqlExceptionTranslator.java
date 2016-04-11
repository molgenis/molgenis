package org.molgenis.data.postgresql;

import static java.lang.String.format;
import static java.util.Collections.singleton;
import static java.util.Objects.requireNonNull;

import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.validation.ConstraintViolation;
import org.molgenis.data.validation.MolgenisValidationException;
import org.postgresql.util.PSQLException;
import org.postgresql.util.ServerErrorMessage;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;

/**
 * Translates PostgreSQL exceptions to MOLGENIS data exceptions
 */
public class PostgreSqlExceptionTranslator extends SQLErrorCodeSQLExceptionTranslator
{
	public PostgreSqlExceptionTranslator(DataSource dataSource)
	{
		super(requireNonNull(dataSource));
	}

	@Override
	protected DataAccessException doTranslate(String task, String sql, SQLException ex)
	{
		DataAccessException dataAccessException = super.doTranslate(task, sql, ex);
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
		switch (pSqlException.getSQLState())
		{
			case "23502": // not_null_violation
				return translateNotNullViolation(pSqlException);
			case "23503": // foreign_key_violation
				return translateForeignKeyViolation(pSqlException);
			case "23505": // unique_violation
				return translateUniqueKeyViolation(pSqlException);
			default:
				break;
		}

		return new MolgenisDataException(dataAccessException);
	}

	/**
	 * Package private for testability
	 * 
	 * @param pSqlException
	 * @return
	 */
	MolgenisValidationException translateNotNullViolation(PSQLException pSqlException)
	{
		ServerErrorMessage serverErrorMessage = pSqlException.getServerErrorMessage();
		String tableName = serverErrorMessage.getTable();
		String message = serverErrorMessage.getMessage();
		Matcher matcher = Pattern.compile("null value in column \"(.*?)\" violates not-null constraint")
				.matcher(message);
		boolean matches = matcher.matches();
		if (!matches)
		{
			throw new RuntimeException("Error translating exception", pSqlException);
		}
		String columnName = matcher.group(1);

		ConstraintViolation constraintViolation = new ConstraintViolation(
				format("The attribute '%s' of entity '%s' can not be null.", columnName, tableName), null);
		return new MolgenisValidationException(singleton(constraintViolation));
	}

	/**
	 * Package private for testability
	 * 
	 * @param pSqlException
	 * @return
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
		String value = m.group(1);
		if (!m.find())
		{
			throw new RuntimeException("Error translating exception", pSqlException);
		}
		String colName = m.group(1);
		ConstraintViolation constraintViolation = new ConstraintViolation(
				format("Unknown xref value '%s' for attribute '%s' of entity '%s'.", value, colName, tableName), null);
		return new MolgenisValidationException(singleton(constraintViolation));
	}

	/**
	 * Package private for testability
	 * 
	 * @param pSqlException
	 * @return
	 */
	MolgenisValidationException translateUniqueKeyViolation(PSQLException pSqlException)
	{
		ServerErrorMessage serverErrorMessage = pSqlException.getServerErrorMessage();
		String tableName = serverErrorMessage.getTable();
		String detailMessage = serverErrorMessage.getDetail();
		Matcher matcher = Pattern.compile("Key \\((.*?)\\)=\\((.*?)\\) already exists.").matcher(detailMessage);
		boolean matches = matcher.matches();
		if (!matches)
		{
			throw new RuntimeException("Error translating exception", pSqlException);
		}
		String columnName = matcher.group(1);
		String value = matcher.group(2);

		ConstraintViolation constraintViolation = new ConstraintViolation(
				format("Duplicate value '%s' for unique attribute '%s' from entity '%s'.", value, columnName,
						tableName),
				null);
		return new MolgenisValidationException(singleton(constraintViolation));
	}
}
