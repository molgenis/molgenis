package org.molgenis.data.security.permission;

public interface RoleMembershipService
{
	void addUserToRole(String username, String roleName);
}
