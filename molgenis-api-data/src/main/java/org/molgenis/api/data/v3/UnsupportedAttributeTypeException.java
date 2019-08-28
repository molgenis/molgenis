package org.molgenis.api.data.v3;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.util.exception.BadRequestException;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class UnsupportedAttributeTypeException extends BadRequestException {
  private static final String ERROR_CODE = "DAPI01";

  private final String attributeName;
  private final AttributeType attributeType;

  UnsupportedAttributeTypeException(Attribute attribute) {
    super(ERROR_CODE);
    this.attributeName = attribute.getName();
    this.attributeType = attribute.getDataType();
  }

  @Override
  public String getMessage() {
    return String.format("name:%s type:%s", attributeName, attributeType.toString());
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {attributeName, attributeType};
  }
}
