package org.molgenis.omx.plugin;

import org.molgenis.framework.ui.IframePlugin;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.omx.controller.DataSetsIndexerController;

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