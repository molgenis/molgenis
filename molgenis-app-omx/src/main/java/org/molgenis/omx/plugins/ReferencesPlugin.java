package org.molgenis.omx.plugins;

import org.molgenis.framework.ui.IframePlugin;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.omx.controller.ReferencesController;

public class ReferencesPlugin extends IframePlugin
{
	private static final long serialVersionUID = 1L;

	public ReferencesPlugin(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}

	@Override
	public String getIframeSrc()
	{
		return ReferencesController.URI;
	}
}
