package org.molgenis.security.captcha;

import org.molgenis.i18n.CodedRuntimeException;

public class CaptchaException extends CodedRuntimeException {
  private static final String ERROR_CODE = "SEC06";

  public CaptchaException() {
    super(ERROR_CODE);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[0];
  }
}
