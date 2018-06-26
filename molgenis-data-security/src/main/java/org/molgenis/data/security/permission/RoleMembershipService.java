package org.molgenis.data.security.permission;

import org.molgenis.data.security.auth.Role;
import org.molgenis.data.security.auth.RoleMembership;

import java.util.Collection;

public interface RoleMembershipService
{
	void addUserToRole(String username, String roleName);

	Collection<RoleMembership> getMemberships(Collection<Role> roles);
}
