package org.molgenis.data.support;

import static java.util.Objects.requireNonNull;

import org.molgenis.i18n.CodedRuntimeException;

class TemplateExpressionInvalidTagException extends CodedRuntimeException {
  private static final String ERROR_CODE = "D12c";

  private final String expression;
  private final String tag;

  TemplateExpressionInvalidTagException(String expression, String tag) {
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
