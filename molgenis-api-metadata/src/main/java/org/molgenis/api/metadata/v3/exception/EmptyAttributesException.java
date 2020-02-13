package org.molgenis.api.metadata.v3.exception;

import org.molgenis.util.exception.BadRequestException;

@SuppressWarnings("java:S110")
public class EmptyAttributesException extends BadRequestException {
  private static final String ERROR_CODE = "MAPI02";

  public EmptyAttributesException() {
    super(ERROR_CODE);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[0];
  }
}
