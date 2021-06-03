package org.molgenis.data.validation;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import org.molgenis.data.meta.model.Attribute;
import org.molgenis.util.exception.CodedRuntimeException;

/** Thrown when an attribute expression fails to parse. */
// S2166 'Classes named like "Exception" should extend "Exception" or a subclass' often gives false
// positives at dev time
@SuppressWarnings({"squid:MaximumInheritanceDepth", "squid:S2166"})
public class AttributeExpressionParseException extends CodedRuntimeException {

  private static final String ERROR_CODE = "VAL01";
  private final String expression;
  private final transient Attribute attribute;
  private final String message;
  private final int index;

  public AttributeExpressionParseException(
      String expression, Attribute attribute, String message, int index) {
    super(ERROR_CODE);
    this.expression = requireNonNull(expression);
    this.attribute = requireNonNull(attribute);
    this.message = requireNonNull(message);
    this.index = index;
  }

  public AttributeExpressionParseException(
      String expression, Attribute attribute, String message, int index, Throwable cause) {
    super(ERROR_CODE, cause);
    this.expression = requireNonNull(expression);
    this.attribute = requireNonNull(attribute);
    this.message = requireNonNull(message);
    this.index = index;
  }

  @Override
  public String getMessage() {
    return format(
        "expression: %s, entityType: %s attribute:%s message:%s, index:%d",
        expression, attribute.getEntity().getId(), attribute.getName(), message, index);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {expression, attribute, message, index};
  }
}
