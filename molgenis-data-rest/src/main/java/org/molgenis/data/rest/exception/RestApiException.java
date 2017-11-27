package org.molgenis.data.rest.exception;

import org.molgenis.data.CodedRuntimeException;

/**
 * abstract exception to group Rest Exceptions.
 */
public abstract class RestApiException extends CodedRuntimeException
{
	protected RestApiException(String errorCode)
	{
		super(errorCode);
	}
}
