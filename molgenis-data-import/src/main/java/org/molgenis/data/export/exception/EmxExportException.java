package org.molgenis.data.export.exception;

import org.molgenis.i18n.CodedRuntimeException;

public class EmxExportException extends CodedRuntimeException {
  private static final String ERROR_CODE = "DL01";

  public EmxExportException(Throwable cause) {
    super(ERROR_CODE, cause);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[0];
  }
}
