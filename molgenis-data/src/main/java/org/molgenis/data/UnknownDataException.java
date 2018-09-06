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
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public abstract class UnknownDataException extends ErrorCodedDataAccessException {
  UnknownDataException(String errorCode) {
    super(errorCode);
  }
}
