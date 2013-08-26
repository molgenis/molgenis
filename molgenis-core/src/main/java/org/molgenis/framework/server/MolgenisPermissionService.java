package org.molgenis.framework.server;

public interface MolgenisPermissionService
{
	boolean hasReadPermissionOnPlugin(String pluginName);

	boolean hasWritePermissionOnPlugin(String pluginName);

	boolean hasReadPermissionOnEntity(String entityName);

	boolean hasWritePermissionOnEntity(String entityName);
}
