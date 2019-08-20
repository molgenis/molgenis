package org.molgenis.data;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class ValueLengthExceededException extends DataConstraintViolationException {
  private static final String ERROR_CODE = "D17";

  public ValueLengthExceededException(@Nullable @CheckForNull Throwable cause) {
    super(ERROR_CODE, cause);
  }

  @Override
  public String getMessage() {
    return null;
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[0];
  }
}
