package org.molgenis.data;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.meta.model.Attribute;
import org.molgenis.util.exception.CodedRuntimeException;
import org.springframework.context.support.DefaultMessageSourceResolvable;

public class InvalidAttributeValueException extends CodedRuntimeException {

  private static final String ERROR_CODE = "D16";
  private final Attribute attribute;
  private final Object expectedType;

  /**
   * @param attribute the attribute for which the exception occurred.
   * @param expectedTypeKey a resolvable message source key for the expetected value type.
   */
  public InvalidAttributeValueException(Attribute attribute, String expectedTypeKey) {
    super(ERROR_CODE);
    this.attribute = requireNonNull(attribute);
    requireNonNull(expectedTypeKey);
    this.expectedType = new DefaultMessageSourceResolvable(expectedTypeKey);
  }

  /**
   * @param attribute the attribute for which the exception occurred.
   * @param expectedType the expected class of the value.
   */
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
        attribute.getName(), attribute.getDataType(), expectedType);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {
      attribute.getName(), attribute.getDataType(), new DefaultMessageSourceResolvable(expectedType)
    };
    return new Object[] {attribute.getName(), attribute.getDataType(), expectedType};
  }
}
