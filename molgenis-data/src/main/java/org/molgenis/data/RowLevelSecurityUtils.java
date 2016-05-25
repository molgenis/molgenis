package org.molgenis.data;

import static org.molgenis.security.core.utils.SecurityUtils.currentUserIsSu;

import java.util.Set;

import org.molgenis.security.core.Permission;
import org.molgenis.security.core.utils.SecurityUtils;

import com.google.common.collect.Sets;

public class RowLevelSecurityUtils
{
	public static boolean validatePermission(Entity entity, Permission permission)
	{
		if (currentUserIsSu()) return true;
		String usernamesStr = entity.getString("_" + permission.toString());
		if (usernamesStr != null)
		{
			Set<String> usernames = Sets.newHashSet(usernamesStr.split(","));
			if (!usernames.contains(SecurityUtils.getCurrentUsername()))
			{
				throw new MolgenisDataAccessException(
						"No " + permission.toString() + " permission on entity with id" + entity.getIdValue());
			}
		}
		return true;
	}
}
