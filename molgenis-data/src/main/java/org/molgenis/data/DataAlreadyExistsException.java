package org.molgenis.data;

import javax.annotation.CheckForNull;

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

  DataAlreadyExistsException(String errorCode, @CheckForNull Throwable cause) {
    super(errorCode, cause);
  }
}
