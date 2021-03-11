package org.molgenis.data.security.permission;

import java.util.Collection;
import org.molgenis.data.security.auth.Role;
import org.molgenis.data.security.auth.VOGroup;
import org.molgenis.data.security.auth.VOGroupRoleMembership;

public interface VOGroupRoleMembershipService {
  Collection<VOGroupRoleMembership> getCurrentMemberships(Collection<VOGroup> groups);

  Collection<VOGroupRoleMembership> getMemberships(Collection<Role> roles);

  void add(VOGroup voGroup, Role role);

  void removeMembership(String id);

  void updateMembership(String id, Role newRole);
}
