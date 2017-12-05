package org.molgenis.data.importer.emx.exception;

import org.molgenis.data.CodedRuntimeException;

public abstract class EmxException extends CodedRuntimeException
{
	protected EmxException(String errorCode)
	{
		super(errorCode);
	}
}
