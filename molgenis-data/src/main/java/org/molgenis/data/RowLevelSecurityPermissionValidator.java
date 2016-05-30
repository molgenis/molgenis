package org.molgenis.data;

import static autovalue.shaded.com.google.common.common.collect.Iterables.isEmpty;
import static java.util.Objects.requireNonNull;
import static org.molgenis.auth.MolgenisUser.USERNAME;
import static org.molgenis.data.RowLevelSecurityRepositoryDecorator.UPDATE_ATTRIBUTE;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;
import static org.molgenis.security.core.utils.SecurityUtils.currentUserIsSu;

import org.molgenis.security.core.Permission;
import org.molgenis.security.core.utils.SecurityUtils;

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
		if (currentUserIsSu()) return true;

		String currentUsername = SecurityUtils.getCurrentUsername();
		return runAsSystem(() -> {
			Iterable<Entity> users = dataService.findOne(entityMetaData.getName(), id).getEntities(UPDATE_ATTRIBUTE);

			if (users != null || !isEmpty(users))
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
