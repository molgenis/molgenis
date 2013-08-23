package org.molgenis.omx.plugins;

import org.molgenis.framework.ui.IframePlugin;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.omx.controller.BackgroundController;

public class BackgroundPlugin extends IframePlugin
{
	private static final long serialVersionUID = 1L;

	public BackgroundPlugin(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}

	@Override
	public String getIframeSrc()
	{
		return BackgroundController.URI;
	}
}
