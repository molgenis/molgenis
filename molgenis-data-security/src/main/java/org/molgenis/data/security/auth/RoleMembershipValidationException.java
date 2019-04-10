package org.molgenis.data.security.auth;

import static java.util.Objects.requireNonNull;

import org.molgenis.i18n.CodedRuntimeException;

public class RoleMembershipValidationException extends CodedRuntimeException {
  private static final String ERROR_CODE = "DS20";

  private final RoleMembership roleMembership;

  public RoleMembershipValidationException(RoleMembership roleMembership) {
    super(ERROR_CODE);
    this.roleMembership = requireNonNull(roleMembership);
  }

  @Override
  protected Object[] getLocalizedMessageArguments() {
    return new Object[0];
  }

  @Override
  public String getMessage() {
    return "roleMembership:" + roleMembership.getId();
  }
}
