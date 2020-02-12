package org.molgenis.data;

/**
 * Thrown when a data or metadata element does not exist.
 *
 * @see UnknownAttributeException
 * @see UnknownEntityTypeException
 * @see UnknownEntityException
 * @see UnknownRepositoryException
 * @see UnknownRepositoryCollectionException
 * @see UnknownPluginException
 * @see UnknownSortAttributeException
 */
@SuppressWarnings("java:MaximumInheritanceDepth")
public abstract class UnknownDataException extends ErrorCodedDataAccessException {
  protected UnknownDataException(String errorCode) {
    super(errorCode);
  }
}
