package org.molgenis.data;

/**
 * Thrown when a data or metadata element does not exist.
 *
 * @see UnknownEntityTypeException
 */
public abstract class UnknownDataException extends ErrorCodedDataAccessException
{
	public UnknownDataException(String errorCode)
	{
		super(errorCode);
	}
}
