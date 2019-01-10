package org.molgenis.data.security.auth;

import static java.util.Objects.requireNonNull;

import org.molgenis.i18n.CodedRuntimeException;

class UserSuModificationException extends CodedRuntimeException {
  private static final String ERROR_CODE = "DS18";
  private final String username;

  UserSuModificationException(User user) {
    super(ERROR_CODE);
    this.username = requireNonNull(user).getUsername();
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
