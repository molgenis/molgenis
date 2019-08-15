package org.molgenis.data.meta;

import org.molgenis.util.exception.BadRequestException;

public class MetadataAccessException extends BadRequestException {
  private static final String ERROR_CODE = "D15";

  public MetadataAccessException() {
    super(ERROR_CODE);
  }

  @Override
  public String getMessage() {
    return null;
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[0];
  }
}
