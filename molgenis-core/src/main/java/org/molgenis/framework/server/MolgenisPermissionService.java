package org.molgenis.framework.server;

import org.molgenis.framework.ui.MolgenisPlugin;
import org.molgenis.util.Entity;

public interface MolgenisPermissionService
{
	public enum Permission
	{
		READ, WRITE, OWN;
	};

	boolean hasPermissionOnPlugin(String pluginName, Permission permission);

	boolean hasPermissionOnPlugin(Class<? extends MolgenisPlugin> pluginClazz, Permission permission);

	void setPermissionOnPlugin(String pluginName, Integer roleId, Permission permission);

	void setPermissionOnPlugin(Class<? extends MolgenisPlugin> pluginClazz, Integer roleId, Permission permission);

	boolean hasPermissionOnEntity(String entityName, Permission permission);

	boolean hasPermissionOnEntity(Class<? extends Entity> entityClazz, Permission permission);

	void setPermissionOnEntity(String entityName, Integer roleId, Permission permission);

	void setPermissionOnEntity(Class<? extends Entity> entityClazz, Integer roleId, Permission permission);
}
