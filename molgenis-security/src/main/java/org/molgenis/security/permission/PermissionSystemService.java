package org.molgenis.security.permission;

import org.molgenis.data.meta.model.EntityType;
import org.springframework.security.core.context.SecurityContext;

import java.util.List;
import java.util.stream.Stream;

/**
 * Updates current user permissions as system user, changes take effect immediately.
 */
public interface PermissionSystemService
{
	void giveUserEntityPermissions(Stream<EntityType> entityTypeStream);

	@Deprecated
	void giveUserEntityPermissions(SecurityContext securityContext, List<String> entities);
}
