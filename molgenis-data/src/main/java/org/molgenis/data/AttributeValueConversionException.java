package org.molgenis.data;

public class AttributeValueConversionException extends RuntimeException {
  AttributeValueConversionException(String message, Throwable t) {
    super(message, t);
  }
}
