package org.molgenis.ui.form;

import org.molgenis.framework.ui.IframePlugin;
import org.molgenis.framework.ui.ScreenController;

public class MolgenisEntityFormPlugin extends IframePlugin
{
	private static final long serialVersionUID = 1L;

	public MolgenisEntityFormPlugin(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}

	@Override
	public String getIframeSrc()
	{
		return MolgenisEntityFormPluginController.URI;
	}

}
