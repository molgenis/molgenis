package org.molgenis.security.core;

public interface PermissionService
{
	boolean hasPermissionOnPlugin(String pluginId, Permission permission);

	boolean hasPermissionOnEntityType(String entityTypeId, Permission permission);
}
