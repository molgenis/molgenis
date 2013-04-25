package org.molgenis.compute.db.plugin;

import org.molgenis.framework.ui.IframePlugin;
import org.molgenis.framework.ui.ScreenController;

public class PilotDashboardPlugin extends IframePlugin
{
	private static final long serialVersionUID = 1L;

	public PilotDashboardPlugin(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}

	@Override
	public String getIframeSrc()
	{
		return "/plugin/dashboard";
	}

}
