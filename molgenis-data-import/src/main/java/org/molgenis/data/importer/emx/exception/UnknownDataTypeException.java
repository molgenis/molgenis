package org.molgenis.data.importer.emx.exception;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import org.molgenis.i18n.CodedRuntimeException;

public class UnknownDataTypeException extends CodedRuntimeException {
  private static final String ERROR_CODE = "IMP18";
  private final String emxDataType;
  private final String emxAttributes;
  private final int rowIndex;

  public UnknownDataTypeException(String emxDataType, String emxAttributes, int rowIndex) {
    super(ERROR_CODE);
    this.emxDataType = requireNonNull(emxDataType);
    this.emxAttributes = requireNonNull(emxAttributes);
    this.rowIndex = rowIndex;
  }

  @Override
  public String getMessage() {
    return format(
        "emxDataType:%s, emxAttributes:%s, rowIndex:%s", emxDataType, emxAttributes, rowIndex);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {emxDataType, emxAttributes, rowIndex};
  }
}
