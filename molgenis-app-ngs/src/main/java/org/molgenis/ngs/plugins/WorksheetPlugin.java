package org.molgenis.ngs.plugins;

import org.molgenis.framework.ui.IframePlugin;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.ngs.controller.WorksheetController;

public class WorksheetPlugin extends IframePlugin
{
	private static final long serialVersionUID = 1L;

	public WorksheetPlugin(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}

	@Override
	public String getIframeSrc()
	{
		return WorksheetController.URI;
	}
}
