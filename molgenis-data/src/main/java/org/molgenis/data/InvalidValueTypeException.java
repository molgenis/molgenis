package org.molgenis.data;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class InvalidValueTypeException extends DataConstraintViolationException {
  private static final String ERROR_CODE = "D20";
  private final String value;
  private final String type;

  public InvalidValueTypeException(
      String value, String type, @Nullable @CheckForNull Throwable cause) {
    super(ERROR_CODE, cause);
    this.value = value;
    this.type = type;
  }

  @Override
  public String getMessage() {
    return "value:" + value + " type:" + type;
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {value, type};
  }
}
