package org.molgenis.dataexplorer.plugin;

import org.molgenis.framework.ui.IframePlugin;
import org.molgenis.framework.ui.ScreenController;

public class DataExplorerPlugin extends IframePlugin
{
	private static final long serialVersionUID = 1L;

	public DataExplorerPlugin(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}

	@Override
	public String getIframeSrc()
	{
		return "/plugin/dataexplorer";
	}

}
