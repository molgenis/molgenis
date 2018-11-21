package org.molgenis.data.security.user;

import static java.util.Objects.requireNonNull;

import org.molgenis.i18n.CodedRuntimeException;

public class UnknownUserException extends CodedRuntimeException {
  private static final String ERROR_CODE = "DS17";
  private final String username;

  public UnknownUserException(String username) {
    super(ERROR_CODE);
    this.username = requireNonNull(username);
  }

  @Override
  public String getMessage() {
    return String.format("username:%s", username);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {username};
  }
}
