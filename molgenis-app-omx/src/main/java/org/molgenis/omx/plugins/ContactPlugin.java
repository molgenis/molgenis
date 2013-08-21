package org.molgenis.omx.plugins;

import org.molgenis.framework.ui.IframePlugin;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.omx.controller.ContactController;

public class ContactPlugin extends IframePlugin
{
	private static final long serialVersionUID = 1L;

	public ContactPlugin(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}

	@Override
	public String getIframeSrc()
	{
		return ContactController.URI;
	}
}
