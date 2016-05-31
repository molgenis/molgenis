package org.molgenis.data;

import static autovalue.shaded.com.google.common.common.collect.Iterables.isEmpty;
import static java.util.Objects.requireNonNull;
import static org.molgenis.auth.MolgenisUser.USERNAME;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;
import static org.molgenis.security.core.utils.SecurityUtils.currentUserHasRole;
import static org.molgenis.security.core.utils.SecurityUtils.currentUserIsSu;
import static org.molgenis.security.core.utils.SecurityUtils.getCurrentUsername;

import org.molgenis.security.core.Permission;
import org.molgenis.security.core.runas.SystemSecurityToken;

public class RowLevelSecurityPermissionValidator
{
	private final DataService dataService;

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

	public boolean validatePermissionById(Object id, EntityMetaData entityMetaData, Permission permission)
	{
		if (!hasPermissionById(id, entityMetaData, permission))
		{
			throw new MolgenisDataAccessException(
					"No " + permission.toString() + " permission on entity with id " + id);
		}
		return true;
	}

	public boolean hasPermission(Entity entity, Permission permission)
	{
		return hasPermissionById(entity.getIdValue(), entity.getEntityMetaData(), permission);
	}

	private boolean hasPermissionById(Object id, EntityMetaData entityMetaData, Permission permission)
	{
		if (currentUserIsSu() || currentUserHasRole(SystemSecurityToken.ROLE_SYSTEM)) return true;

		String currentUsername = getCurrentUsername();
		return runAsSystem(() -> {
			Iterable<Entity> users = dataService.findOne(entityMetaData.getName(), id)
					.getEntities("_" + permission.toString());

			if (users != null && !isEmpty(users))
			{
				for (Entity user : users)
				{
					if (user.getString(USERNAME).equals(currentUsername))
					{
						return true;
					}
				}
			}
			return false;
		});
	}
}