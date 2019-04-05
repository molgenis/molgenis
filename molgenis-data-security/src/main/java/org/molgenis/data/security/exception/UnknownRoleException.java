package org.molgenis.data.security.exception;

import static java.util.Objects.requireNonNull;

import org.molgenis.i18n.CodedRuntimeException;

public class UnknownRoleException extends CodedRuntimeException {
  private static final String ERROR_CODE = "DS19";
  private final String name;

  public UnknownRoleException(String name) {
    super(ERROR_CODE);
    this.name = requireNonNull(name);
  }

  @Override
  public String getMessage() {
    return String.format("name:%s", name);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {name};
  }
}
