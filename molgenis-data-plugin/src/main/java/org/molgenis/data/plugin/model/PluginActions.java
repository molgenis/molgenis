package org.molgenis.data.plugin.model;

import org.molgenis.security.core.Action;

public enum PluginActions implements Action
{
	VIEW_PLUGIN
			{
				public String getName()
				{
					return "VIEW_PLUGIN";
				}

				;
			}
}
