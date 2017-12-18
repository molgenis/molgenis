package org.molgenis.data.annotation.core.exception;

import org.molgenis.i18n.CodedRuntimeException;

import static java.util.Objects.requireNonNull;

public class InvalidNumberOfGenesException extends CodedRuntimeException
{
	private static final String ERROR_CODE = "AN03";
	private final int sourceEntitiesSize;

	public InvalidNumberOfGenesException(int sourceEntitiesSize)
	{
		super(ERROR_CODE);
		this.sourceEntitiesSize = requireNonNull(sourceEntitiesSize);
	}

	@Override
	public String getMessage()
	{
		return String.format("sourceEntitiesSize:%d", sourceEntitiesSize);
	}

	@Override
	protected Object[] getLocalizedMessageArguments()
	{
		return new Object[] { sourceEntitiesSize };
	}
}
