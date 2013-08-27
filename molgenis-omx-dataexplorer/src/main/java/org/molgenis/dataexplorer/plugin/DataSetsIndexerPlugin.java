package org.molgenis.dataexplorer.plugin;

import org.molgenis.dataexplorer.controller.DataSetsIndexerController;
import org.molgenis.framework.ui.IframePlugin;
import org.molgenis.framework.ui.ScreenController;

public class DataSetsIndexerPlugin extends IframePlugin
{
	private static final long serialVersionUID = 1L;

	public DataSetsIndexerPlugin(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}

	@Override
	public String getIframeSrc()
	{
		return DataSetsIndexerController.URI;
	}
}