package org.molgenis.api.convert;

import static java.lang.String.format;

import org.molgenis.api.model.Query.Operator;
import org.molgenis.util.exception.CodedRuntimeException;

public class MissingRsqlValueException extends CodedRuntimeException {
  private static final String ERROR_CODE = "API04";
  private final Operator operator;

  public MissingRsqlValueException(Operator operator) {
    super(ERROR_CODE);

    this.operator = operator;
  }

  @Override
  public String getMessage() {
    return format("operator: %s", operator);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {operator};
  }
}
