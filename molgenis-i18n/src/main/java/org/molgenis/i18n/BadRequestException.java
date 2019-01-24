package org.molgenis.i18n;

public abstract class BadRequestException extends CodedRuntimeException {

  protected BadRequestException(String errorCode) {
    super(errorCode);
  }

  protected BadRequestException(String errorCode, Throwable cause) {
    super(errorCode, cause);
  }
}
