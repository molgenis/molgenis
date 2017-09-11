package org.molgenis.data.security;

import org.molgenis.security.core.Permission;

import java.util.EnumSet;

public interface PermissionService
{
	boolean isEntityLevelSecurity(String entityTypeId); // TODO can we use EntityType here?

	/**
	 * @param entityTypeId
	 * @return attribute name (not identifier)
	 */
	String getEntityLevelSecurityInheritance(String entityTypeId); // TODO return Attribute

	EnumSet<Permission> getPermissions(Object entityId); // TODO replace entityId with entity

	boolean hasPermissionOnPlugin(String pluginId, Permission permission);

	boolean hasPermissionOnEntityType(String entityTypeId, Permission permission);

	boolean hasPermissionOnEntity(String entityTypeId, Object entityId, Permission permission);
}
