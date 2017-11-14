package org.molgenis.data.validation;

import org.molgenis.data.DataAccessException;

import javax.annotation.Nullable;

/**
 * {@link org.springframework.dao.DataIntegrityViolationException} with error code and without message.
 */
public abstract class DataIntegrityViolationException extends DataAccessException
{
	public DataIntegrityViolationException(String errorCode)
	{
		super(errorCode);
	}

	public DataIntegrityViolationException(String errorCode, @Nullable Throwable cause)
	{
		super(errorCode, cause);
	}
}
