package org.molgenis.data;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

/**
 * Thrown when a data or metadata element already exist.
 *
 * @see EntityAlreadyExistsException
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public abstract class DataAlreadyExistsException extends ErrorCodedDataAccessException {
  public DataAlreadyExistsException(String errorCode) {
    super(errorCode);
  }

  DataAlreadyExistsException(String errorCode, @Nullable @CheckForNull Throwable cause) {
    super(errorCode, cause);
  }
}
