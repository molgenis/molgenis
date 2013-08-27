package org.molgenis.framework.server;

public interface MolgenisPermissionService
{
	public enum Permission
	{
		READ, WRITE, OWN;
	};

	boolean hasPermissionOnPlugin(String pluginName, Permission permission);

	boolean hasPermissionOnEntity(String entityName, Permission permission);

	// void setPermissionOnPlugin(String pluginName, Permission permission, Integer roleId);

	// void setPermissionOnEntity(String entityName, Permission permission, Integer roleId);
}
