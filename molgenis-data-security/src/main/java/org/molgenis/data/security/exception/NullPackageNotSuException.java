package org.molgenis.data.security.exception;

import org.molgenis.util.exception.CodedRuntimeException;

public class NullPackageNotSuException extends CodedRuntimeException {
  private static final String ERROR_CODE = "DS02";

  public NullPackageNotSuException() {
    super(ERROR_CODE);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new String[] {};
  }
}
