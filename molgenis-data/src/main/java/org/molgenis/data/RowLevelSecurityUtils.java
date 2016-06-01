package org.molgenis.data;

import static autovalue.shaded.com.google.common.common.collect.Iterables.isEmpty;
import static org.molgenis.auth.MolgenisUser.USERNAME;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

import org.molgenis.security.core.Permission;
import org.molgenis.security.core.runas.SystemSecurityToken;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.security.core.Authentication;

public class RowLevelSecurityUtils
{
	public static boolean validatePermission(Entity completeEntity, Permission permission,
			Authentication authentication)
	{
		if (!hasPermission(completeEntity, permission, authentication))
		{
			throw new MolgenisDataAccessException(
					"No " + permission.toString() + " permission on entity with id " + completeEntity.getIdValue());
		}
		return true;
	}

	public static boolean hasPermission(Entity completeEntity, Permission permission, Authentication authentication)
	{
		if (SecurityUtils.userIsSu(authentication)
				|| SecurityUtils.userHasRole(authentication, SystemSecurityToken.ROLE_SYSTEM))
		{
			return true;
		}

		String username = SecurityUtils.getUsername(authentication);

		return runAsSystem(() -> {
			Iterable<Entity> users = completeEntity.getEntities("_" + permission.toString());

			if (users != null && !isEmpty(users))
			{
				for (Entity user : users)
				{
					if (user.getString(USERNAME).equals(username))
					{
						return true;
					}
				}
			}
			return false;
		});
	}
}