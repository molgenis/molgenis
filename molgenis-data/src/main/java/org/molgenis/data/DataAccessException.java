package org.molgenis.data;

import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

/**
 * {@link org.springframework.dao.DataAccessException} with error code and without message.
 */
public class DataAccessException extends org.springframework.dao.DataAccessException implements ErrorCoded
{
	private final String errorCode;

	public DataAccessException(String errorCode)
	{
		this(errorCode, null);
	}

	public DataAccessException(String errorCode, @Nullable Throwable cause)
	{
		super("", cause);
		this.errorCode = requireNonNull(errorCode);
	}

	@Override
	public String getErrorCode()
	{
		return errorCode;
	}
}
