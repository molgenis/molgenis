package org.molgenis.data.support.exceptions;

import static java.util.Objects.requireNonNull;

import org.molgenis.util.exception.CodedRuntimeException;

public class TemplateExpressionUnknownAttributeException extends CodedRuntimeException {
  private static final String ERROR_CODE = "D12b";

  private final String expression;
  private final String tag;

  public TemplateExpressionUnknownAttributeException(String expression, String tag) {
    super(ERROR_CODE);
    this.expression = requireNonNull(expression);
    this.tag = requireNonNull(tag);
  }

  @Override
  public String getMessage() {
    return String.format("expression:%s tag:%s", expression, tag);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {expression, tag};
  }
}
