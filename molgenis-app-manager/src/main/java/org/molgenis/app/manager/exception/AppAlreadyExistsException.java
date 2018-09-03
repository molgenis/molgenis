package org.molgenis.app.manager.exception;

import static java.util.Objects.requireNonNull;

import org.molgenis.i18n.CodedRuntimeException;

public class AppAlreadyExistsException extends CodedRuntimeException {
  private static final String ERROR_CODE = "AM08";
  private final String appUri;

  public AppAlreadyExistsException(String appUri) {
    super(ERROR_CODE);
    this.appUri = requireNonNull(appUri);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {appUri};
  }

  @Override
  public String getMessage() {
    return appUri;
  }
}
