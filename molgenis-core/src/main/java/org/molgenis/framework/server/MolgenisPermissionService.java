package org.molgenis.framework.server;

public interface MolgenisPermissionService
{
	public enum Permission
	{
		READ, WRITE, COUNT;
	};

	boolean hasPermissionOnPlugin(String pluginId, Permission permission);

	boolean hasPermissionOnEntity(String entityName, Permission permission);
}
