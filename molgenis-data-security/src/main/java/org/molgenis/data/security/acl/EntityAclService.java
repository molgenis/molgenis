package org.molgenis.data.security.acl;

import org.molgenis.data.meta.model.EntityType;
import org.molgenis.security.core.Permission;

import java.util.Collection;

/**
 * Entity access control list retrieval.
 */
public interface EntityAclService
{
	/**
	 * Returns access control list for the given entity identity containing entries for the authenticated user.
	 *
	 * @return entity access control list
	 * @throws org.molgenis.data.MolgenisDataAccessException if no ACL exists for this entity
	 */
	EntityAcl readAcl(EntityIdentity entityIdentity);

	/**
	 * Returns access control list for the given entities containing entries for the authenticated user.
	 *
	 * @return entity access control list or <tt>null</tt> if no access control list exists for the given entity.
	 * @throws org.molgenis.data.MolgenisDataAccessException if no ACL exists for one or more entities
	 */
	Collection<EntityAcl> readAcls(Collection<EntityIdentity> entityIdentities);

	/**
	 * Returns whether authorization is granted to a given entity with a given permission for the authenticated user.
	 *
	 * @return <tt>true</tt> if authorization is granted
	 */
	boolean isGranted(EntityIdentity entityIdentity, Permission permission);

	/**
	 * Returns whether an ACL class exists for the given entity type.
	 */
	boolean hasAclClass(EntityType entityType);

	String getAclClassParent(EntityType entityType); // TODO docs and check whether this is good enough
}
