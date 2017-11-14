package org.molgenis.data;

/**
 * Thrown when a data or metadata element does not exist.
 *
 * @see UnknownAttributeException
 * @see UnknownEntityException
 * @see UnknownEntityTypeException
 */
public abstract class UnknownDataException extends DataAccessException
{
	public UnknownDataException(String errorCode)
	{
		super(errorCode);
	}
}
