package org.molgenis.omx.auth;

import org.molgenis.framework.server.MolgenisPermissionService;
import org.molgenis.framework.ui.MolgenisPlugin;
import org.molgenis.util.Entity;

/**
 * Permission service based on the OMX model
 */
@Deprecated
public class OmxPermissionService implements MolgenisPermissionService
{

	@Override
	public void setPermissionOnPlugin(Class<? extends MolgenisPlugin> pluginClazz, Integer roleId, Permission permission)
	{
	}

	@Override
	public void setPermissionOnPlugin(String pluginName, Integer roleId, Permission permission)
	{
	}

	@Override
	public void setPermissionOnEntity(Class<? extends Entity> entityClazz, Integer roleId, Permission permission)
	{
	}

	@Override
	public void setPermissionOnEntity(String entityName, Integer roleId, Permission permission)
	{
	}

	@Override
	public boolean hasPermissionOnPlugin(Class<? extends MolgenisPlugin> pluginClazz, Permission permission)
	{
		return true;
	}

	@Override
	public boolean hasPermissionOnPlugin(String pluginName, Permission permission)
	{
		return true;
	}

	@Override
	public boolean hasPermissionOnEntity(Class<? extends Entity> entityClazz, Permission permission)
	{
		return true;
	}

	@Override
	public boolean hasPermissionOnEntity(String entityName, Permission permission)
	{
		return true;
	}
}
