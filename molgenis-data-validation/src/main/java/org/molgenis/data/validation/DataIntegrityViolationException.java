package org.molgenis.data.validation;

import org.molgenis.data.ErrorCodedDataAccessException;

import javax.annotation.Nullable;

/**
 * {@link org.springframework.dao.DataIntegrityViolationException} with error code and without message.
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public abstract class DataIntegrityViolationException extends ErrorCodedDataAccessException
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
