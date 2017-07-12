package org.molgenis.data.security.acl;

import org.molgenis.data.Entity;
import org.molgenis.security.core.Permission;

import java.util.List;

/**
 * Entity access control list retrieval.
 */
public interface EntityAclService
{
	/**
	 * Returns access control list for the given entity.
	 *
	 * @param entity entity
	 * @return entity access control list or <tt>null</tt> if no access control list exists for the given entity.
	 */
	EntityAcl readAcl(Entity entity);

	/**
	 * Returns access control list for the given entity containing access control entries for the given security ids.
	 *
	 * @param entity      entity
	 * @param securityIds security identifiers (may be <tt>null</tt> to retrieve all entries)
	 * @return entity access control list or <tt>null</tt> if no access control list exists for the given entity.
	 */
	EntityAcl readAcl(Entity entity, List<SecurityId> securityIds);

	/**
	 * Returns whether authorization is granted to a given entity for one or more permissions for one or more security
	 * identities.
	 *
	 * @param entity      entity
	 * @param permissions permission or permissions required (at least one required)
	 * @param securityIds security identifiers (at least one required)
	 * @return <tt>true</tt> if authorization is granted
	 */
	boolean isGranted(Entity entity, List<Permission> permissions, List<SecurityId> securityIds);
}
