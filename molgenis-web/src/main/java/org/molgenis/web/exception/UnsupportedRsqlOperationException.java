package org.molgenis.web.exception;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.util.exception.CodedRuntimeException;

/** Thrown when a combination of operator and attribute type is not supported. */
// S2166 'Classes named like "Exception" should extend "Exception" or a subclass' often gives false
// positives at dev time
@SuppressWarnings({"squid:MaximumInheritanceDepth", "squid:S2166"})
public class UnsupportedRsqlOperationException extends CodedRuntimeException {

  private static final String ERROR_CODE = "WEB03";
  private final String operator;
  private final transient EntityType entityType;
  private final transient Attribute attribute;

  public UnsupportedRsqlOperationException(
      String operator, EntityType entityType, Attribute attribute) {
    super(ERROR_CODE);
    this.operator = requireNonNull(operator);
    this.entityType = requireNonNull(entityType);
    this.attribute = requireNonNull(attribute);
  }

  public UnsupportedRsqlOperationException(
      String operator, EntityType entityType, Attribute attribute, Throwable cause) {
    super(ERROR_CODE, cause);
    this.operator = requireNonNull(operator);
    this.entityType = requireNonNull(entityType);
    this.attribute = requireNonNull(attribute);
  }

  @Override
  public String getMessage() {
    return format(
        "operator:%s, entityType: %s, attribute:%s, dataType:%s",
        operator, entityType.getId(), attribute.getName(), attribute.getDataType());
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {operator, attribute.getDataType(), attribute, entityType};
  }
}
