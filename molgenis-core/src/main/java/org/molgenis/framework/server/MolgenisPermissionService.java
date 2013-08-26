package org.molgenis.framework.server;

public interface MolgenisPermissionService
{
	boolean hasReadPermissionOnPlugin(String pluginClassName);

	boolean hasWritePermissionOnPlugin(String pluginClassName);

	boolean hasReadPermissionOnEntity(String entityName);

	boolean hasWritePermissionOnEntity(String entityName);
}
