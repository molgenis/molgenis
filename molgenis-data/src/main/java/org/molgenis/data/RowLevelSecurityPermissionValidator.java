package org.molgenis.data;

import static autovalue.shaded.com.google.common.common.collect.Iterables.isEmpty;
import static java.util.Objects.requireNonNull;
import static org.molgenis.auth.MolgenisUser.USERNAME;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;
import static org.molgenis.security.core.utils.SecurityUtils.currentUserIsSu;

import org.molgenis.security.core.Permission;

public class RowLevelSecurityPermissionValidator
{
	private final DataService dataService;

	public RowLevelSecurityPermissionValidator(DataService dataService)
	{
		this.dataService = requireNonNull(dataService);
	}

	public boolean validatePermission(Entity entity, Permission permission, String username)
	{
		if (!hasPermission(entity, permission, username))
		{
			throw new MolgenisDataAccessException(
					"No " + permission.toString() + " permission on entity with id " + entity.getIdValue());
		}
		return true;
	}

	public boolean validatePermissionById(Object id, EntityMetaData entityMetaData, Permission permission,
			String username)
	{
		if (!hasPermissionById(id, entityMetaData, permission, username))
		{
			throw new MolgenisDataAccessException(
					"No " + permission.toString() + " permission on entity with id " + id);
		}
		return true;
	}

	public boolean hasPermission(Entity entity, Permission permission, String username)
	{
		return hasPermissionById(entity.getIdValue(), entity.getEntityMetaData(), permission, username);
	}

	private boolean hasPermissionById(Object id, EntityMetaData entityMetaData, Permission permission, String username)
	{
		if (currentUserIsSu()) return true;

		return runAsSystem(() -> {
			Iterable<Entity> users = dataService.findOne(entityMetaData.getName(), id)
					.getEntities("_" + permission.toString());

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