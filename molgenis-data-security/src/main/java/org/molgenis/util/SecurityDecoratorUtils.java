package org.molgenis.util;

import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.security.exception.EntityTypePermissionDeniedException;
import org.molgenis.security.core.Permission;

import static java.lang.String.format;
import static org.molgenis.security.core.runas.SystemSecurityToken.ROLE_SYSTEM;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_SU;
import static org.molgenis.security.core.utils.SecurityUtils.currentUserHasRole;

public class SecurityDecoratorUtils
{
	private SecurityDecoratorUtils()
	{
	}

	public static void validatePermission(EntityType entityType, Permission permission)
	{
		String role = format("ROLE_ENTITY_%s_%s", permission.toString(), entityType.getId());
		if (!currentUserHasRole(AUTHORITY_SU, ROLE_SYSTEM, role))
		{
			throw new EntityTypePermissionDeniedException(entityType, permission);
		}
	}
}
