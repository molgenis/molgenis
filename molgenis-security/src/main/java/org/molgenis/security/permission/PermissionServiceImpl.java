package org.molgenis.security.permission;

import org.molgenis.security.core.Permission;
import org.molgenis.security.core.PermissionService;
import org.springframework.stereotype.Component;

@Component
public class PermissionServiceImpl implements PermissionService
{
	@Override
	public boolean hasPermissionOnPlugin(String pluginId, Permission permission)
	{
		// TODO: implement
		return true;
	}

	@Override
	public boolean hasPermissionOnEntityType(String entityTypeId, Permission permission)
	{
		// TODO: implement
		return true;
	}

	@Override
	public boolean hasPermissionOnMappingProject(String mappingProjectId, Permission permission)
	{
		// TODO: implement
		return true;
	}
}
