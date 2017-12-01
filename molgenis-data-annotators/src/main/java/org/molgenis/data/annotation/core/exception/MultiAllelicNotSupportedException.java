package org.molgenis.data.annotation.core.exception;

import org.molgenis.data.CodedRuntimeException;

public class MultiAllelicNotSupportedException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "AN02";

	public MultiAllelicNotSupportedException()
	{
		super(ERROR_CODE);
	}
}
