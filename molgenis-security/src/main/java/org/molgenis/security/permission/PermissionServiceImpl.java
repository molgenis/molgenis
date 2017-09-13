package org.molgenis.security.permission;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.security.PermissionService;
import org.molgenis.data.security.acl.EntityAclService;
import org.molgenis.data.security.acl.EntityIdentity;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toCollection;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.plugin.model.PluginMetadata.PLUGIN;
import static org.molgenis.security.core.Permission.NONE;
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
	public boolean isEntityLevelSecurity(EntityType entityType)
	{
		return entityAclService.hasAclClass(entityType);
	}

	@Override
	public Attribute getEntityLevelSecurityInheritance(EntityType entityType)
	{
		String attributeName = entityAclService.getAclClassParent(entityType);
		return attributeName != null ? entityType.getAttribute(attributeName) : null;
	}

	@Override
	public Set<Permission> getPermissions(Entity entity)
	{
		EntityIdentity entityIdentity = EntityIdentity.create(entity.getEntityType().getId(), entity.getIdValue());

		// TODO performance bottleneck? if yes, find better solution
		// TODO remove NONE condition, see EntityAclManagerImpl
		// TODO decide whether we want to check for su/system here or in EntityAclService
		return Arrays.stream(Permission.values())
					 .filter(permission -> permission != NONE && (SecurityUtils.currentUserIsSuOrSystem()
							 || entityAclService.isGranted(entityIdentity, permission)))
					 .collect(toCollection(() -> EnumSet.noneOf(Permission.class)));
	}

	@Override
	public boolean hasPermissionOnPlugin(String pluginId, Permission permission)
	{
		return hasPermissionOnEntity(PLUGIN, pluginId, permission);
	}

	@Override
	public boolean hasPermissionOnEntity(Entity entity, Permission permission)
	{
		return hasPermissionOnEntity(entity.getEntityType().getId(), entity.getIdValue(), permission);
	}

	@Override
	public boolean hasPermissionOnEntityType(String entityTypeId, Permission permission)
	{
		return hasPermissionOnEntity(ENTITY_TYPE_META_DATA, entityTypeId, permission);
	}

	@Override
	public boolean hasPermissionOnEntityType(EntityType entityType, Permission permission)
	{
		return hasPermissionOnEntity(ENTITY_TYPE_META_DATA, entityType.getId(), permission);
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
