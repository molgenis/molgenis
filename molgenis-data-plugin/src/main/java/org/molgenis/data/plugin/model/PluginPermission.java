package org.molgenis.data.plugin.model;

import org.molgenis.security.core.Permission;

import static java.util.Objects.requireNonNull;

public enum PluginPermission implements Permission
{
	VIEW_PLUGIN("Permission to view this plugin");

	private String defaultDescription;

	PluginPermission(String defaultDescription)
	{
		this.defaultDescription = requireNonNull(defaultDescription);
	}

	@Override
	public String getDefaultDescription()
	{
		return defaultDescription;
	}
}