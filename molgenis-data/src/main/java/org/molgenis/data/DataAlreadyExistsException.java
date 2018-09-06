package org.molgenis.data;

import javax.annotation.Nullable;

/**
 * Thrown when a data or metadata element already exist.
 *
 * @see EntityAlreadyExistsException
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public abstract class DataAlreadyExistsException extends ErrorCodedDataAccessException {
  DataAlreadyExistsException(String errorCode) {
    super(errorCode);
  }

  DataAlreadyExistsException(String errorCode, @Nullable Throwable cause) {
    super(errorCode, cause);
  }
}
