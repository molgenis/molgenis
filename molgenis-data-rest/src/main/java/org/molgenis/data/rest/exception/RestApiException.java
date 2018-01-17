package org.molgenis.data.rest.exception;

import org.molgenis.i18n.CodedRuntimeException;

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
