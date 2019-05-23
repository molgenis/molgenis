package org.molgenis.data.security.exception;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class ReadPermissionDeniedException extends PermissionDeniedException {
  private static final String ERROR_CODE = "DS24";

  private final String typeId;

  public ReadPermissionDeniedException(String typeId) {
    super(ERROR_CODE);
    this.typeId = requireNonNull(typeId);
  }

  @Override
  public String getMessage() {
    return String.format("typeId:%s", typeId);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {typeId};
  }
}
