package org.molgenis.data.importer.emx.exception;

import org.molgenis.i18n.CodedRuntimeException;

public abstract class EmxException extends CodedRuntimeException
{
	protected EmxException(String errorCode)
	{
		super(errorCode);
	}

	protected EmxException(String errorCode, Exception cause)
	{
		super(errorCode, cause);
	}
}
