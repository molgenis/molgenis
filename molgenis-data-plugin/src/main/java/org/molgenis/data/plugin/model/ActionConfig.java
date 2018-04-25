package org.molgenis.data.plugin.model;

import org.molgenis.security.core.ActionPermissionMappingRegistry;
import org.molgenis.security.core.Permission;
import org.springframework.context.annotation.Configuration;

import static java.util.Objects.requireNonNull;

@Configuration
public class ActionConfig
{
	private final ActionPermissionMappingRegistry actionPermissionMappingRegistry;

	public ActionConfig(ActionPermissionMappingRegistry actionPermissionMappingRegistry)
	{
		this.actionPermissionMappingRegistry = requireNonNull(actionPermissionMappingRegistry);

		addActionsToRegistry();
	}

	private void addActionsToRegistry()
	{
		actionPermissionMappingRegistry.addMapping(PluginAction.VIEW_PLUGIN, Permission.READ);
	}
}
