package org.molgenis.data;

public class AttributeValueConversionException extends RuntimeException {
  public AttributeValueConversionException(String message) {
    super(message);
  }

  public AttributeValueConversionException(String message, Throwable t) {
    super(message, t);
  }
}
