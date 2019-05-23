package org.molgenis.data.security.exception;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class InsufficientInheritancePermissionsException extends PermissionDeniedException {
  private static final String ERROR_CODE = "DS28";

  public InsufficientInheritancePermissionsException() {
    super(ERROR_CODE);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[] {};
  }
}
