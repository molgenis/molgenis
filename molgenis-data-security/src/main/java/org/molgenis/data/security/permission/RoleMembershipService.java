package org.molgenis.data.security.permission;

import java.util.Collection;
import org.molgenis.data.security.auth.Role;
import org.molgenis.data.security.auth.RoleMembership;
import org.molgenis.data.security.auth.User;

public interface RoleMembershipService {
  void addUserToRole(String username, String roleName);

  void addUserToRole(final User user, final Role role);

  Collection<RoleMembership> getMemberships(Collection<Role> roles);

  void removeMembership(final RoleMembership roleMembership);

  void updateMembership(RoleMembership roleMembership, Role newRole);
}
