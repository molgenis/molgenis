package org.molgenis.data;

import static java.util.Objects.requireNonNull;

/**
 * {@link RuntimeException} with error code.
 */
public class CodedRuntimeException extends RuntimeException implements ErrorCoded
{
	private final String errorCode;

	protected CodedRuntimeException(String errorCode)
	{
		this.errorCode = requireNonNull(errorCode);
	}

	protected CodedRuntimeException(String message, String errorCode)
	{
		super(message);
		this.errorCode = errorCode;
	}

	protected CodedRuntimeException(String message, Throwable cause, String errorCode)
	{
		super(message, cause);
		this.errorCode = errorCode;
	}

	protected CodedRuntimeException(Throwable cause, String errorCode)
	{
		super(cause);
		this.errorCode = errorCode;
	}

	@Override
	public String getErrorCode()
	{
		return errorCode;
	}
}
