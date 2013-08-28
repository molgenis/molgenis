package org.molgenis.omx.auth.ui;

import org.molgenis.framework.ui.IframePlugin;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.omx.auth.controller.UserAccountController;

public class UserAccountPlugin extends IframePlugin
{
	private static final long serialVersionUID = 1L;

	public UserAccountPlugin(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}

	@Override
	public String getIframeSrc()
	{
		return UserAccountController.URI;
	}
}
