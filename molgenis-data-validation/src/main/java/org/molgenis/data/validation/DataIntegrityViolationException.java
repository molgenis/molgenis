package org.molgenis.data.validation;

import org.molgenis.data.ErrorCoded;

import javax.annotation.Nullable;

/**
 * {@link org.springframework.dao.DataIntegrityViolationException} with error code and without message.
 */
public abstract class DataIntegrityViolationException extends org.springframework.dao.DataIntegrityViolationException
		implements ErrorCoded
{
	private final String errorCode;

	public DataIntegrityViolationException(String errorCode)
	{
		this(errorCode, null);
	}

	public DataIntegrityViolationException(String errorCode, @Nullable Throwable cause)
	{
		super("", cause);
		this.errorCode = errorCode;
	}

	@Override
	public String getErrorCode()
	{
		return errorCode;
	}
}
