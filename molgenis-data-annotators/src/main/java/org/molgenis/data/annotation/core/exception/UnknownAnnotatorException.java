package org.molgenis.data.annotation.core.exception;

import org.molgenis.data.CodedRuntimeException;

public class UnknownAnnotatorException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "AN09";
	private String annotatorName;

	public UnknownAnnotatorException(String annotatorName)
	{
		super(ERROR_CODE);
		this.annotatorName = annotatorName;
	}
}
