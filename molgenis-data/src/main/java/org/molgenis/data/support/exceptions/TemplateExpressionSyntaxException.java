package org.molgenis.data.support.exceptions;

import static java.util.Objects.requireNonNull;

import org.molgenis.util.exception.CodedRuntimeException;

public class TemplateExpressionSyntaxException extends CodedRuntimeException {
  private static final String ERROR_CODE = "D12e";

  private final String expression;

  public TemplateExpressionSyntaxException(String expression, Exception e) {
    super(ERROR_CODE, e);
    this.expression = requireNonNull(expression);
  }

  @Override
  public String getMessage() {
    return String.format("expression:%s", expression);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {expression};
  }
}
