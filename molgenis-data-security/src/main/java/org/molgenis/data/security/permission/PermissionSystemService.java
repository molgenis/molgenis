package org.molgenis.data.security.permission;

import org.molgenis.data.meta.model.EntityType;

import java.util.Collection;

/**
 * Updates current user permissions as system user, changes take effect immediately.
 *
 * @deprecated use {@link org.springframework.security.acls.model.MutableAclService}
 */
@Deprecated
public interface PermissionSystemService
{
	void giveUserWriteMetaPermissions(EntityType entityType);

	void giveUserWriteMetaPermissions(Collection<EntityType> entityTypes);
}
