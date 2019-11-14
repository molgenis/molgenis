package org.molgenis.api.metadata.v3.exception;

import org.molgenis.util.exception.BadRequestException;

public class IdModificationException extends BadRequestException {

  private static final String ERROR_CODE = "MAPI03";

  public IdModificationException() {
    super(ERROR_CODE);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[0];
  }
}
