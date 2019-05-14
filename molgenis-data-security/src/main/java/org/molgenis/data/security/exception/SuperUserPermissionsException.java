package org.molgenis.data.security.exception;

import static java.util.Objects.requireNonNull;

import org.molgenis.i18n.CodedRuntimeException;

public class SuperUserPermissionsException extends CodedRuntimeException {
  private static final String ERROR_CODE = "DS32";

  private final String name;

  public SuperUserPermissionsException(String name) {
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
