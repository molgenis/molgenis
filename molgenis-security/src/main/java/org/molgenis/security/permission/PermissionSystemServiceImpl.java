package org.molgenis.security.permission;

import org.molgenis.data.meta.model.EntityType;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.stereotype.Component;

import java.util.Collection;

import static java.util.Collections.singleton;

@Component
public class PermissionSystemServiceImpl implements PermissionSystemService
{

	public PermissionSystemServiceImpl()
	{
	}

	@Override
	public void giveUserWriteMetaPermissions(EntityType entityType)
	{
		giveUserWriteMetaPermissions(singleton(entityType));
	}

	@Override
	public void giveUserWriteMetaPermissions(Collection<EntityType> entityTypes)
	{
		// superusers and system user have all permissions by default
		if (SecurityUtils.currentUserIsSuOrSystem())
		{
			return;
		}

		throw new UnsupportedOperationException("EntityType permissions not yet implemented");
	}

}
