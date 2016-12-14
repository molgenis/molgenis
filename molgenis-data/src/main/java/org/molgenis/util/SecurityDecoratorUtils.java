package org.molgenis.util;

import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.security.core.Permission;

import static java.lang.String.format;
import static org.molgenis.security.core.utils.SecurityUtils.currentUserHasRole;

public class SecurityDecoratorUtils
{

	public static void validatePermission(String entityName, Permission permission)
	{
		String role = format("ROLE_ENTITY_%s_%s", permission.toString(), entityName);
		if (!currentUserHasRole("ROLE_SU", "ROLE_SYSTEM", role))
		{
			throw new MolgenisDataAccessException(
					format("No [%s] permission on entity [%s]", permission.toString(), entityName));
		}
	}

	public static boolean isPermissionValid(String entityName, Permission permission)
	{
		String role = format("ROLE_ENTITY_%s_%s", permission.toString(), entityName);
		return currentUserHasRole("ROLE_SU", "ROLE_SYSTEM", role);
	}

}
