package org.molgenis.data.importer.emx.exception;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import org.molgenis.i18n.CodedRuntimeException;

public class InvalidValueException extends CodedRuntimeException {
  private static final String ERROR_CODE = "IMP09";
  private final String value;
  private final String identifier;
  private final String allowedValue;
  private final String sheet;
  private final int rowIndex;

  public InvalidValueException(
      String value, String identifier, String allowedValue, String sheet, int rowIndex) {
    super(ERROR_CODE);
    this.value = requireNonNull(value);
    this.identifier = requireNonNull(identifier);
    this.allowedValue = requireNonNull(allowedValue);
    this.sheet = requireNonNull(sheet);
    this.rowIndex = requireNonNull(rowIndex);
  }

  @Override
  public String getMessage() {
    return format(
        "value:%s, identifier:%s, allowedValue:%s, sheet:%s, rowIndex:%s",
        value, identifier, allowedValue, sheet, rowIndex);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {value, identifier, allowedValue, sheet, rowIndex};
  }
}
