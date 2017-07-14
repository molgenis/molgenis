package org.molgenis.security.permission;

import org.molgenis.data.security.acl.EntityAclService;
import org.molgenis.data.security.acl.EntityIdentity;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.core.Permission;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.security.core.utils.SecurityUtils.currentUserIsSuOrSystem;
import static org.molgenis.ui.PluginMetadata.PLUGIN;

@Component
public class MolgenisPermissionServiceImpl implements MolgenisPermissionService
{
	private final EntityAclService entityAclService;

	MolgenisPermissionServiceImpl(EntityAclService entityAclService)
	{
		this.entityAclService = requireNonNull(entityAclService);
	}

	@Override
	public boolean hasPermissionOnPlugin(String pluginId, Permission permission)
	{
		return hasPermissionOnEntity(PLUGIN, pluginId, permission);
	}

	@Override
	public boolean hasPermissionOnEntity(String entityTypeId, Permission permission)
	{
		return hasPermissionOnEntity(ENTITY_TYPE_META_DATA, entityTypeId, permission);
	}

	private boolean hasPermissionOnEntity(String entityTypeId, Object entityId, Permission permission)
	{
		boolean hasPermission;
		if (currentUserIsSuOrSystem())
		{
			hasPermission = true;
		}
		else
		{
			EntityIdentity entityIdentity = EntityIdentity.create(entityTypeId, entityId);
			hasPermission = entityAclService.isGranted(entityIdentity, permission);
		}
		return hasPermission;
	}
}
