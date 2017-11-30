package org.molgenis.data.importer.exception;

import org.molgenis.i18n.CodedRuntimeException;

public abstract class ImporterException extends CodedRuntimeException
{
	public ImporterException(String errorCode)
	{
		super(errorCode);
	}
}


