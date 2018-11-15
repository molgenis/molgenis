package org.molgenis.data.export.exception;

import org.molgenis.i18n.CodedRuntimeException;

public class EmptyExportRequestException extends CodedRuntimeException {
  private static final String ERROR_CODE = "EXP02";

  public EmptyExportRequestException () {
    super(ERROR_CODE);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[0];
  }
}
