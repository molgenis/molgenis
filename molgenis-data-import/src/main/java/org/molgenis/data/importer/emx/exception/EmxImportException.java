package org.molgenis.data.importer.emx.exception;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import org.molgenis.i18n.CodedRuntimeException;

public class EmxImportException extends CodedRuntimeException {
  private static final String ERROR_CODE = "IMP03";
  private final Throwable cause;
  private final String sheet;
  private final int rowIndex;

  public EmxImportException(Throwable cause, String sheet, int rowIndex) {
    super(ERROR_CODE, cause);
    this.cause = requireNonNull(cause);
    this.sheet = requireNonNull(sheet);
    this.rowIndex = rowIndex;
  }

  @Override
  public String getMessage() {
    return format("sheet:%s rowIndex:%s", sheet, rowIndex);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {cause.getLocalizedMessage(), sheet, rowIndex};
  }
}
