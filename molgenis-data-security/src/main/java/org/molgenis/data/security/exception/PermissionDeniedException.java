package org.molgenis.data.security.exception;

import org.molgenis.i18n.CodedRuntimeException;

public abstract class PermissionDeniedException extends CodedRuntimeException
{
	PermissionDeniedException(String errorCode)
	{
		super(errorCode);
	}

	PermissionDeniedException(String errorCode, Throwable cause)
	{
		super(errorCode, cause);
	}
}