package org.molgenis.data;

import org.springframework.core.convert.ConversionException;

class DataConversionException extends RuntimeException {

  DataConversionException(String message) {
    super(message);
  }

  DataConversionException(ConversionException e) {
    super(e);
  }
}
