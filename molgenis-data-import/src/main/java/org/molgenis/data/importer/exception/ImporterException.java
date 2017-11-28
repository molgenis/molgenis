package org.molgenis.data.importer.exception;

import org.molgenis.data.CodedRuntimeException;

public abstract class ImporterException extends CodedRuntimeException
{
	public ImporterException(String errorCode)
	{
		super(errorCode);
	}
}


