package org.molgenis.security.permission;

import org.molgenis.data.meta.model.EntityType;

import java.util.Collection;

/**
 * Updates current user permissions as system user, changes take effect immediately.
 */
public interface PermissionSystemService
{
	void giveUserEntityPermissions(EntityType entityType);

	void giveUserEntityPermissions(Collection<EntityType> entityTypes);
}
