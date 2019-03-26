package org.molgenis.security.account.exception;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import org.molgenis.i18n.CodedRuntimeException;

public class EmailAlreadyExistsException extends CodedRuntimeException {
  private static final String ERROR_CODE = "SEC01";
  private String email;

  public EmailAlreadyExistsException(String email) {
    super(ERROR_CODE);
    this.email = requireNonNull(email);
  }

  @Override
  public String getMessage() {
    return format("email:%s", email);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {email};
  }
}
