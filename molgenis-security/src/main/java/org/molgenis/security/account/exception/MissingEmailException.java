package org.molgenis.security.account.exception;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import org.molgenis.i18n.CodedRuntimeException;

public class MissingEmailException extends CodedRuntimeException {
  private static final String ERROR_CODE = "SEC04";
  private String username;

  public MissingEmailException(String username) {
    super(ERROR_CODE);
    this.username = requireNonNull(username);
  }

  @Override
  public String getMessage() {
    return format("username:%s", username);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {username};
  }
}
