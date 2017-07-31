package org.molgenis.security.core;

public interface PermissionService
{
	boolean hasPermissionOnPlugin(String pluginId, Permission permission);

	boolean hasPermissionOnEntity(String entityTypeId, Permission permission);
}
