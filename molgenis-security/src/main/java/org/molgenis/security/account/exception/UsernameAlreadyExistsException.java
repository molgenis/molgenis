package org.molgenis.security.account.exception;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import org.molgenis.i18n.CodedRuntimeException;

public class UsernameAlreadyExistsException extends CodedRuntimeException {
  private static final String ERROR_CODE = "SEC03";
  private String username;

  public UsernameAlreadyExistsException(String username) {
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
