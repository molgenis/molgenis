package org.molgenis.security.permission;

import org.molgenis.data.security.PermissionService;
import org.molgenis.data.security.acl.EntityAclService;
import org.molgenis.data.security.acl.EntityIdentity;
import org.molgenis.security.core.Permission;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.plugin.model.PluginMetadata.PLUGIN;
import static org.molgenis.security.core.utils.SecurityUtils.currentUserIsSuOrSystem;

@Component
public class PermissionServiceImpl implements PermissionService
{
	private final EntityAclService entityAclService;

	public PermissionServiceImpl(EntityAclService entityAclService)
	{
		this.entityAclService = requireNonNull(entityAclService);
	}

	@Override
	public boolean isEntityLevelSecurity(String entityTypeId)
	{
		throw new UnsupportedOperationException("FIXME"); // FIXME implement
	}

	@Override
	public String getEntityLevelSecurityInheritance(String entityTypeId)
	{
		throw new UnsupportedOperationException("FIXME"); // FIXME implement
	}

	@Override
	public EnumSet<Permission> getPermissions(Object entityId)
	{
		throw new UnsupportedOperationException("FIXME"); // FIXME implement
	}

	@Override
	public boolean hasPermissionOnPlugin(String pluginId, Permission permission)
	{
		return hasPermissionOnEntity(PLUGIN, pluginId, permission);
	}

	@Override
	public boolean hasPermissionOnEntityType(String entityTypeId, Permission permission)
	{
		return hasPermissionOnEntity(ENTITY_TYPE_META_DATA, entityTypeId, permission);
	}

	@Override
	public boolean hasPermissionOnEntity(String entityTypeId, Object entityId, Permission permission)
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
