package org.molgenis.data.support;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.i18n.CodedRuntimeException;

class TemplateExpressionAttributeTypeException extends CodedRuntimeException {
  private static final String ERROR_CODE = "D12a";

  private final String expression;
  private final String tag;
  private final AttributeType attributeType;

  TemplateExpressionAttributeTypeException(String expression, String tag, Attribute tagAttribute) {
    super(ERROR_CODE);
    this.expression = requireNonNull(expression);
    this.tag = requireNonNull(tag);
    this.attributeType = requireNonNull(tagAttribute).getDataType();
  }

  @Override
  public String getMessage() {
    return String.format("expression:%s tag:%s type:%s", expression, tag, attributeType);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {expression, tag, attributeType.toString()};
  }
}
