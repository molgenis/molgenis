package org.molgenis.data;

import static java.util.Objects.requireNonNull;

/**
 * {@link RuntimeException} with error code.
 */
public class CodedRuntimeException extends RuntimeException implements CodedException
{
	private final String errorCode;

	protected CodedRuntimeException(String errorCode)
	{
		this.errorCode = requireNonNull(errorCode);
	}

	@Override
	public String getErrorCode()
	{
		return errorCode;
	}
}
