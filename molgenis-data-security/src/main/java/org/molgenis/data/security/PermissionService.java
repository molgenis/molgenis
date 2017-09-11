package org.molgenis.data.security;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.security.core.Permission;

import java.util.Set;

public interface PermissionService
{
	boolean isEntityLevelSecurity(EntityType entityType);

	Attribute getEntityLevelSecurityInheritance(EntityType entityType);

	Set<Permission> getPermissions(Entity entity); // TODO replace entityId with entity

	boolean hasPermissionOnPlugin(String pluginId, Permission permission);

	boolean hasPermissionOnEntityType(String entityTypeId, Permission permission);

	boolean hasPermissionOnEntity(Entity entity, Permission permission);

	boolean hasPermissionOnEntityType(EntityType entityType, Permission permission);

}
