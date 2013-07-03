package org.molgenis.protocolmanager.plugin;

import org.molgenis.framework.ui.IframePlugin;
import org.molgenis.framework.ui.ScreenController;

public class ProtocolManagerPlugin extends IframePlugin
{
	private static final long serialVersionUID = 1L;

	public ProtocolManagerPlugin(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}

	@Override
	public String getIframeSrc()
	{
		return "/plugin/protocolmanager";
	}

}
