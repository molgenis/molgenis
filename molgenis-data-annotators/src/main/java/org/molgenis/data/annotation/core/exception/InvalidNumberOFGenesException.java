package org.molgenis.data.annotation.core.exception;

import org.molgenis.data.CodedRuntimeException;

public class InvalidNumberOFGenesException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "AN03";
	private int sourceEntitiesSize;

	public InvalidNumberOFGenesException(int sourceEntitiesSize)
	{
		super(ERROR_CODE);
		this.sourceEntitiesSize = sourceEntitiesSize;
	}
}
