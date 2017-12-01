package org.molgenis.data.annotation.core.exception;

import org.molgenis.data.CodedRuntimeException;

public class UnsupportedQueryException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "AN04";
	private String field;

	public UnsupportedQueryException(String field)
	{
		super(ERROR_CODE);
		this.field = field;
	}
}
