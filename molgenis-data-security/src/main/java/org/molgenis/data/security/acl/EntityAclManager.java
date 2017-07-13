package org.molgenis.data.security.acl;

import org.molgenis.data.Entity;

/**
 * Entity access control list creation, retrieval, updates and deletions.
 */
public interface EntityAclManager extends EntityAclService
{
	/**
	 * Create entity access control list with access control entry for the current user (unless the current user is a
	 * superuser or system user).
	 *
	 * @return entity access control list
	 */
	EntityAcl createAcl(Entity entity);

	/**
	 * Update existing entity access control list
	 */
	void updateAcl(EntityAcl entityAcl);

	/**
	 * Delete entity access control list for the given entity.
	 */
	void deleteAcl(EntityIdentity entityIdentity);
}
