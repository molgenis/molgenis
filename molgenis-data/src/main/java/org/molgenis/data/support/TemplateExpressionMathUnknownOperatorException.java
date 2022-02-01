package org.molgenis.data.support;

import org.molgenis.util.exception.CodedRuntimeException;

public class TemplateExpressionMathUnknownOperatorException extends CodedRuntimeException {
  private static final String ERROR_CODE = "D12g";

  private final String operator;

  TemplateExpressionMathUnknownOperatorException(String operator) {
    super(ERROR_CODE);
    this.operator = operator;
  }

  @Override
  public String getMessage() {
    return String.format("operator:%s", operator);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {operator};
  }
}
