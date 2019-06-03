package org.molgenis.api.permissions.exceptions.rsql;

import org.molgenis.i18n.BadRequestException;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class UnsupportedPermissionQueryOperatorException extends BadRequestException {
  private static final String ERROR_CODE = "PRM02";

  public UnsupportedPermissionQueryOperatorException() {
    super(ERROR_CODE);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[0];
  }
}
