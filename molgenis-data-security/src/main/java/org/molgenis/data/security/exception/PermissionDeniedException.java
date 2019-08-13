package org.molgenis.data.security.exception;

import org.molgenis.util.exception.CodedRuntimeException;

public abstract class PermissionDeniedException extends CodedRuntimeException {
  public PermissionDeniedException(String errorCode) {
    super(errorCode);
  }

  public PermissionDeniedException(String errorCode, Throwable cause) {
    super(errorCode, cause);
  }
}
