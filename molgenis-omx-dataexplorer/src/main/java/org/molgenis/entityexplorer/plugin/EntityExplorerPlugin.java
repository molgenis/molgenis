package org.molgenis.entityexplorer.plugin;

import org.molgenis.framework.ui.IframePlugin;
import org.molgenis.framework.ui.ScreenController;

public class EntityExplorerPlugin extends IframePlugin
{
	private static final long serialVersionUID = 1L;

	public EntityExplorerPlugin(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}

	@Override
	public String getIframeSrc()
	{
		return "/plugin/entityexplorer";
	}

}
