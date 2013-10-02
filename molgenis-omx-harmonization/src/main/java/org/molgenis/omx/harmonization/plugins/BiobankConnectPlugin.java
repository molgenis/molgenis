package org.molgenis.omx.harmonization.plugins;

import org.molgenis.framework.ui.IframePlugin;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.omx.harmonization.controllers.BiobankConnectController;

public class BiobankConnectPlugin extends IframePlugin
{
	private static final long serialVersionUID = 1L;

	public BiobankConnectPlugin(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}

	@Override
	public String getIframeSrc()
	{
		return BiobankConnectController.URI;
	}
}
