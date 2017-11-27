package org.molgenis.data.mapper.exception;

import org.molgenis.data.CodedRuntimeException;

public class MappingServiceException extends CodedRuntimeException
{
	protected MappingServiceException(String errorCode)
	{
		super(errorCode);
	}
}
