package org.molgenis.data.rest.exception;

import org.molgenis.data.CodedRuntimeException;

public class RestApiException extends CodedRuntimeException
{
	protected RestApiException(String errorCode)
	{
		super(errorCode);
	}
}
