package org.molgenis.data.security.acl;

import org.molgenis.security.core.Permission;

/**
 * Entity access control list retrieval.
 */
public interface EntityAclService
{
	/**
	 * Returns access control list for the given entity containing entries for the authenticated user.
	 *
	 * @return entity access control list or <tt>null</tt> if no access control list exists for the given entity.
	 */
	EntityAcl readAcl(EntityIdentity entityIdentity);

	/**
	 * Returns whether authorization is granted to a given entity with a given permission for the authenticated user.
	 *
	 * @return <tt>true</tt> if authorization is granted
	 */
	boolean isGranted(EntityIdentity entityIdentity, Permission permission);
}
