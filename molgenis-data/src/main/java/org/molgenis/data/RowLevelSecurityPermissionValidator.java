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

	public boolean validatePermission(Entity completeEntity, Permission permission, String username)
	{
		if (!hasPermission(completeEntity, permission, username))
		{
			throw new MolgenisDataAccessException(
					"No " + permission.toString() + " permission on entity with id " + completeEntity.getIdValue());
		}
		return true;
	}

	public boolean hasPermission(Entity completeEntity, Permission permission, String username)
	{
		if (currentUserIsSu()) return true;

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