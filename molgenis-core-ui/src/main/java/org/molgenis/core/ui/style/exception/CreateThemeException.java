package org.molgenis.core.ui.style.exception;

import org.molgenis.util.exception.CodedRuntimeException;

public class CreateThemeException extends CodedRuntimeException {

  private static final String ERROR_CODE = "CU02";
  private final String fileName;

  public CreateThemeException(String fileName, Throwable cause) {

    super(ERROR_CODE, cause);
    this.fileName = fileName;
  }

  @Override
  public String getMessage() {
    return String.format("fileName:%s", fileName);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {fileName};
  }
}
