package org.molgenis.data;

import org.springframework.core.convert.ConversionException;

class DataConversionException extends RuntimeException {

  DataConversionException(ConversionException e) {
    super(e);
  }

  @Override
  public String getMessage() {
    return getCause().getMessage();
  }
}
