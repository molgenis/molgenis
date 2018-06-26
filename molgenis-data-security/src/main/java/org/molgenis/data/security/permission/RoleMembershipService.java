package org.molgenis.data.security.permission;

import org.molgenis.data.security.auth.Role;
import org.molgenis.data.security.auth.RoleMembership;
import org.molgenis.data.security.auth.User;

import java.util.Collection;

public interface RoleMembershipService
{
	void addUserToRole(String username, String roleName);

	void addUserToRole(final User user, final Role role);

	Collection<RoleMembership> getMemberships(Collection<Role> roles);
}
