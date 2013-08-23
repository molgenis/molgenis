package org.molgenis.omx.protocolviewer;

import org.molgenis.framework.ui.IframePlugin;
import org.molgenis.framework.ui.ScreenController;

public class ProtocolViewerPlugin extends IframePlugin
{
	private static final long serialVersionUID = 1L;

	public ProtocolViewerPlugin(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}

	@Override
	public String getIframeSrc()
	{
		return ProtocolViewerController.URI;
	}

}
