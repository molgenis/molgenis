package org.molgenis.data.security.exception;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.UnknownDataException;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class UnknownTypeException extends UnknownDataException {
  private static final String ERROR_CODE = "DS33";
  private final String type;

  public UnknownTypeException(String type) {
    super(ERROR_CODE);

    this.type = requireNonNull(type);
  }

  @Override
  public String getMessage() {
    return String.format("typeId:%s", type);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {type};
  }
}
