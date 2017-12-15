package org.molgenis.data.mapper.exception;

import org.molgenis.i18n.CodedRuntimeException;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public abstract class MappingServiceException extends CodedRuntimeException
{
	MappingServiceException(String errorCode)
	{
		super(errorCode);
	}

	MappingServiceException(String errorCode, Throwable cause)
	{
		super(errorCode, cause);
	}
}
