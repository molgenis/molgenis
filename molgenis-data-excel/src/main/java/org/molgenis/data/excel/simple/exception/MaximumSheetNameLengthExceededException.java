package org.molgenis.data.excel.simple.exception;

import static java.util.Objects.requireNonNull;

import org.molgenis.i18n.CodedRuntimeException;

public class MaximumSheetNameLengthExceededException extends CodedRuntimeException {
  private static final String ERROR_CODE = "XLS01";
  private final String sheetName;

  public MaximumSheetNameLengthExceededException(String sheetName) {
    super(ERROR_CODE);
    this.sheetName = requireNonNull(sheetName);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {sheetName};
  }
}
