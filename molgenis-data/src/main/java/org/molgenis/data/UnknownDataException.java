package org.molgenis.data;

/**
 * Thrown when a data or metadata element does not exist.
 *
 * @see UnknownAttributeException
 * @see UnknownEntityException
 * @see UnknownEntityTypeException
 * @see UnknownPackageException
 * @see UnknownRepositoryException
 * @see UnknownRepositoryCollectionException
 */
public abstract class UnknownDataException extends ErrorCodedDataAccessException
{
	public UnknownDataException(String errorCode)
	{
		super(errorCode);
	}
}
