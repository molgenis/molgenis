package org.molgenis.data.security.exception;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.UnknownDataException;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class UnknownRoleException extends UnknownDataException {
  private static final String ERROR_CODE = "DS21";
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
