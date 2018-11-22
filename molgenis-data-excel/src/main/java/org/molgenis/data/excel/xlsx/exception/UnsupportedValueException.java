package org.molgenis.data.excel.xlsx.exception;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import org.molgenis.i18n.CodedRuntimeException;

public class UnsupportedValueException extends CodedRuntimeException {
  private static final String ERROR_CODE = "XLS02";
  private final Object value;
  private final Class<? extends Object> claZZ;

  public UnsupportedValueException(Object value) {
    super(ERROR_CODE);
    this.value = requireNonNull(value);
    this.claZZ = requireNonNull(value.getClass());
  }

  @Override
  public String getMessage() {
    return format("value:%s class:%s", value, claZZ.getSimpleName());
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {value, claZZ.getSimpleName()};
  }
}
