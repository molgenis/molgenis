package org.molgenis.data;

import org.molgenis.security.core.Permission;
import org.molgenis.security.core.runas.RunAsSystemProxy;

import static autovalue.shaded.com.google.common.common.collect.Iterables.isEmpty;
import static java.util.Objects.requireNonNull;
import static org.molgenis.auth.MolgenisUser.USERNAME;
import static org.molgenis.data.RowLevelSecurityRepositoryDecorator.UPDATE_ATTRIBUTE;
import static org.molgenis.security.core.utils.SecurityUtils.currentUserIsSu;
import static org.molgenis.security.core.utils.SecurityUtils.getCurrentUsername;
import static org.molgenis.security.core.utils.SecurityUtils.getEntityAuthorities;

public class RowLevelSecurityPermissionValidator
{
	private DataService dataService;

	public RowLevelSecurityPermissionValidator(DataService dataService)
	{
		this.dataService = requireNonNull(dataService);
	}

	public boolean validatePermission(Entity entity, Permission permission)
	{
		if (!userHasUpdatePermissionOnEntity(entity, permission))
		{
			throw new MolgenisDataAccessException(
					"No " + permission.toString() + " permission on entity with id " + entity.getIdValue());
		}
		return true;
	}

	public boolean userHasUpdatePermissionOnEntity(Entity entity, Permission permission)
	{
		if (currentUserIsSu()) return true;

		String entityName = entity.getEntityMetaData().getName();
		if(!getEntityAuthorities(entityName).contains(Permission.UPDATE)) return false;

		String currentUsername = getCurrentUsername();
		return RunAsSystemProxy.runAsSystem(() -> {
			Iterable<Entity> users = dataService.findOne(entityName, entity.getIdValue())
					.getEntities(UPDATE_ATTRIBUTE);

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
