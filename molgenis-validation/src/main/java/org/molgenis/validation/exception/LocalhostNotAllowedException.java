package org.molgenis.validation.exception;

import org.molgenis.i18n.CodedRuntimeException;

public class LocalhostNotAllowedException extends CodedRuntimeException {
  private static final String ERROR_CODE = "V04";

  public LocalhostNotAllowedException() {
    super(ERROR_CODE);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[0];
  }
}
