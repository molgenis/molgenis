package org.molgenis.data.security.acl;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;

import java.util.Collection;

/**
 * Entity access control list creation, retrieval, updates and deletions.
 */
public interface EntityAclManager extends EntityAclService
{
	/**
	 * Create entity access control list class.
	 */
	void createAclClass(EntityType entityType);

	/**
	 * Delete entity access control list class including all related access control lists.
	 */
	void deleteAclClass(EntityType entityType);

	/**
	 * Create entity access control list with access control entry for the current user (unless the current user is a
	 * superuser or system user).
	 */
	void createAcl(Entity entity);

	/**
	 * Same as {@link #createAcl(Entity)} but executed for an entity collection.
	 */
	void createAcls(Collection<Entity> entities);

	/**
	 * Update existing entity access control list
	 */
	void updateAcl(EntityAcl entityAcl);

	/**
	 * Same as {@link #updateAcl(EntityAcl)} but executed for an entity collection.
	 */
	void updateAcls(Collection<EntityAcl> entityAcls);

	/**
	 * Delete entity access control list for the given entity.
	 */
	void deleteAcl(Entity entity);

	/**
	 * Same as {@link #deleteAcl(Entity)} but executed for an entity collection.
	 */
	void deleteAcls(Collection<Entity> entities);

	/**
	 * Delete entity access control list for the given entity identity.
	 */
	void deleteAcl(EntityIdentity entityIdentity);
}
