package org.molgenis.util.exception;

public abstract class ForbiddenException extends CodedRuntimeException {

  protected ForbiddenException(String errorCode) {
    super(errorCode);
  }

  protected ForbiddenException(String errorCode, Throwable cause) {
    super(errorCode, cause);
  }
}
