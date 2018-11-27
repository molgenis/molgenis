package org.molgenis.data.excel.xlsx.exception;

import static java.util.Objects.requireNonNull;

import org.molgenis.i18n.CodedRuntimeException;

public class XlsxWriterException extends CodedRuntimeException {
  private static final String ERROR_CODE = "XLS03";
  private final Throwable cause;

  public XlsxWriterException(Throwable cause) {
    super(ERROR_CODE, cause);
    this.cause = requireNonNull(cause);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {cause.getLocalizedMessage()};
  }
}
