package org.molgenis.data.meta;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class IllegalAttributeTypeException extends RuntimeException {
  private final AttributeType attributeType;

  public IllegalAttributeTypeException(AttributeType attributeType) {
    this.attributeType = requireNonNull(attributeType);
  }

  @Override
  public String getMessage() {
    return format("Illegal attribute type [%s]", attributeType.toString());
  }
}
