package org.molgenis.security;

import org.molgenis.data.security.auth.Role;
import org.molgenis.data.security.auth.User;
import org.molgenis.security.core.PermissionSet;
import org.springframework.security.acls.model.ObjectIdentity;

import java.util.Map;

public interface PermissionService
{
	void grant(Map<ObjectIdentity, PermissionSet> objectPermissionMap, Role role);

	void grant(ObjectIdentity objectIdentity, PermissionSet permissionSet, Role role);

	void grant(Map<ObjectIdentity, PermissionSet> objectPermissionMap, User user);

	void grant(ObjectIdentity objectIdentity, PermissionSet permissionSet, User user);
}
