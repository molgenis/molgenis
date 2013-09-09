package org.molgenis.catalogmanager;

import org.molgenis.framework.ui.IframePlugin;
import org.molgenis.framework.ui.ScreenController;

public class CatalogManagerPlugin extends IframePlugin
{
	private static final long serialVersionUID = 1L;

	public CatalogManagerPlugin(String name, ScreenController<?> parent)
	{
		super(name, parent);

	}

	@Override
	public String getIframeSrc()
	{
		return CatalogManagerController.URI;
	}
}
