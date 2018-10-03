package org.molgenis.data.support;

import static java.util.Objects.requireNonNull;

import org.molgenis.i18n.CodedRuntimeException;

class TemplateExpressionUnknownAttributeException extends CodedRuntimeException {
  private static final String ERROR_CODE = "D12b";

  private final String expression;
  private final String tag;

  TemplateExpressionUnknownAttributeException(String expression, String tag) {
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
