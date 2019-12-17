package org.molgenis.app.manager.exception;

import static java.util.Objects.requireNonNull;

import org.molgenis.util.exception.CodedRuntimeException;

public class CouldNotUploadAppException extends CodedRuntimeException {
  private static final String ERROR_CODE = "AM11";
  private final String fileName;

  public CouldNotUploadAppException(String fileName) {
    super(ERROR_CODE);
    this.fileName = requireNonNull(fileName);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {fileName};
  }

  @Override
  public String getMessage() {
    return fileName;
  }
}
