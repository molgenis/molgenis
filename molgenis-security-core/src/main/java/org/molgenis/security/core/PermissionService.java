package org.molgenis.security.core;

/**
 * Evaluates if the current user has a permission on a resource.
 */
public interface PermissionService
{
	boolean hasPermissionOnPlugin(String pluginId, Permission permission);

	boolean hasPermissionOnEntityType(String entityTypeId, Permission permission);

	boolean hasPermissionOnMappingProject(String mappingProjectId, Permission permission);
}
