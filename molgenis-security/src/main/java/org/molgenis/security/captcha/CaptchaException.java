package org.molgenis.security.captcha;

import org.molgenis.util.exception.CodedRuntimeException;

/** @deprecated use class that extends from {@link CodedRuntimeException} */
@Deprecated
public class CaptchaException extends Exception {
  private static final long serialVersionUID = 1L;

  public CaptchaException(String message) {
    super(message);
  }
}
