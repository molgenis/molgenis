package org.molgenis.data.security.util;

import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.security.core.Permission;

import static java.lang.String.format;
import static org.molgenis.security.core.runas.SystemSecurityToken.ROLE_SYSTEM;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_SU;
import static org.molgenis.security.core.utils.SecurityUtils.currentUserHasRole;

public class SecurityDecoratorUtils
{
	public static void validatePermission(EntityType entityType, Permission permission)
	{
		String role = format("ROLE_ENTITY_%s_%s", permission.toString(), entityType.getId());
		if (!currentUserHasRole(AUTHORITY_SU, ROLE_SYSTEM, role))
		{
			throw new MolgenisDataAccessException(
					format("No [%s] permission on entity type [%s] with id [%s]", permission.toString(),
							entityType.getLabel(), entityType.getId()));
		}
	}
}
