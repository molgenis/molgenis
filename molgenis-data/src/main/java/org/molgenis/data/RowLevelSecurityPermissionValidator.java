package org.molgenis.data;

import static java.util.Objects.requireNonNull;
import static org.molgenis.security.core.utils.SecurityUtils.currentUserIsSu;

import java.util.Set;

import org.molgenis.security.core.Permission;
import org.molgenis.security.core.runas.RunAsSystemProxy;
import org.molgenis.security.core.utils.SecurityUtils;

import com.google.common.collect.Sets;

public class RowLevelSecurityPermissionValidator
{
	private DataService dataService;

	public RowLevelSecurityPermissionValidator(DataService dataService)
	{
		this.dataService = requireNonNull(dataService);
	}

	public boolean validatePermission(Entity entity, Permission permission)
	{
		if (!hasPermission(entity, permission))
		{
			throw new MolgenisDataAccessException(
					"No " + permission.toString() + " permission on entity with id " + entity.getIdValue());
		}
		return true;
	}

	public boolean hasPermission(Entity entity, Permission permission)
	{
		if (currentUserIsSu()) return true;

		String usernamesStr = RunAsSystemProxy
				.runAsSystem(() -> dataService.findOne(entity.getEntityMetaData().getName(), entity.getIdValue())
						.getString("_" + permission.toString()));

		if (usernamesStr != null)
		{
			Set<String> usernames = Sets.newHashSet(usernamesStr.split(","));
			if (usernames.contains(SecurityUtils.getCurrentUsername()))
			{
				return true;
			}
		}
		return false;
	}
}
