package org.molgenis.api.convert;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import org.molgenis.util.exception.CodedRuntimeException;

public class UnknownQueryOperatorException extends CodedRuntimeException {
  private static final String ERROR_CODE = "API05";

  private final String operator;

  public UnknownQueryOperatorException(String operator) {
    super(ERROR_CODE);
    this.operator = requireNonNull(operator);
  }

  @Override
  public String getMessage() {
    return format("operator:%s", operator);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {operator};
  }
}
