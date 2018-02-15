package org.molgenis.security.core;

import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;

public interface PermissionService
{
	/**
	 * Returns whether the current authenticated user has permission on the given domain object.
	 *
	 * @param type       target's type
	 * @param id         identifier for the object instance
	 * @param permission permission granted to a {@link Sid} for a given domain object.
	 * @return <tt>true</tt> if the permission is granted, <tt>false</tt> otherwise
	 */
	boolean hasPermission(String type, String id, Permission permission);
}
