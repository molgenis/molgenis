package org.molgenis.data;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.meta.model.Attribute;
import org.molgenis.util.exception.CodedRuntimeException;
import org.springframework.context.support.DefaultMessageSourceResolvable;

public class InvalidAttributeValueException extends CodedRuntimeException {

  private static final String ERROR_CODE = "D15";
  private final Attribute attribute;
  private final String expectedType;

  public InvalidAttributeValueException(Attribute attribute, String expectedTypeName) {
    super(ERROR_CODE);
    this.attribute = requireNonNull(attribute);
    this.expectedType = requireNonNull(expectedTypeName);
  }

  public InvalidAttributeValueException(Attribute attribute, Class expectedType) {
    super(ERROR_CODE);
    this.attribute = requireNonNull(attribute);
    requireNonNull(expectedType);
    this.expectedType = expectedType.getSimpleName();
  }

  @Override
  public String getMessage() {
    return String.format(
        "attribute:%s datatype:%s expected value type:%s",
        attribute.getName(),
        attribute.getDataType(),
        new DefaultMessageSourceResolvable(expectedType));
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {
      attribute.getName(), attribute.getDataType(), new DefaultMessageSourceResolvable(expectedType)
    };
  }
}
