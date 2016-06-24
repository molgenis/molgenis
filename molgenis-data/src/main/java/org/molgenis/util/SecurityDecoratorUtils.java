package org.molgenis.util;

import static org.molgenis.security.core.utils.SecurityUtils.currentUserHasRole;

import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.security.core.Permission;

public class SecurityDecoratorUtils
{

	public static void validatePermission(String entityName, Permission permission)
	{
		String role = String.format("ROLE_ENTITY_%s_%s", permission.toString(), entityName.toUpperCase());
		if (!currentUserHasRole("ROLE_SU", "ROLE_SYSTEM", role))
		{
			throw new MolgenisDataAccessException("No " + permission.toString() + " permission on entity " + entityName);
		}
	}

	public static boolean isPermissionValid(String entityName, Permission permission)
	{
		String role = String.format("ROLE_ENTITY_%s_%s", permission.toString(), entityName.toUpperCase());
		return currentUserHasRole("ROLE_SU", "ROLE_SYSTEM", role);
	}

}
