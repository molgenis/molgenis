package org.molgenis.data;

/**
 * Thrown when a data or metadata element does not exist.
 *
 * @see UnknownAttributeException
 * @see UnknownEntityTypeException
 * @see UnknownRepositoryException
 * @see UnknownRepositoryCollectionException
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public abstract class UnknownDataException extends ErrorCodedDataAccessException
{
	UnknownDataException(String errorCode)
	{
		super(errorCode);
	}
}
