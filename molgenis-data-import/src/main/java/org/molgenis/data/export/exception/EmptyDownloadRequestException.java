package org.molgenis.data.export.exception;

import org.molgenis.i18n.CodedRuntimeException;

public class EmptyDownloadRequestException extends CodedRuntimeException {
  private static final String ERROR_CODE = "DL02";

  public EmptyDownloadRequestException() {
    super(ERROR_CODE);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[0];
  }
}
