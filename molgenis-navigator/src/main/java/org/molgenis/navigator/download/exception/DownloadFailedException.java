package org.molgenis.navigator.download.exception;

import static java.util.Objects.requireNonNull;

import org.molgenis.i18n.CodedRuntimeException;

public class DownloadFailedException extends CodedRuntimeException {
  private static final String ERROR_CODE = "NAV01";
  private final Throwable cause;

  public DownloadFailedException(Throwable cause) {
    super(ERROR_CODE, cause);
    this.cause = requireNonNull(cause);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {cause.getLocalizedMessage()};
  }
}
