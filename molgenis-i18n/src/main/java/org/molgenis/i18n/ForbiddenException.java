package org.molgenis.i18n;

public abstract class ForbiddenException extends CodedRuntimeException {

  protected ForbiddenException(String errorCode) {
    super(errorCode);
  }

  protected ForbiddenException(String errorCode, Throwable cause) {
    super(errorCode, cause);
  }
}
