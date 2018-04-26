package org.molgenis.data.plugin.model;

import org.molgenis.security.core.Action;

public enum PluginAction implements Action
{
	VIEW_PLUGIN;

	public String getName()
	{
		return name();
	}
}