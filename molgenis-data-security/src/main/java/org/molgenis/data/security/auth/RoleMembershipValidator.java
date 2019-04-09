package org.molgenis.data.security.auth;

public interface RoleMembershipValidator {
  void validate(RoleMembership roleMembership);
}
