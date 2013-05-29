package org.molgenis.lifelines.plugins;

import org.molgenis.framework.ui.IframePlugin;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.lifelines.catalogue.CatalogLoaderController;

/**
 * CatalogueLoader plugin.
 * 
 * IFramePluging for the CatalogLoaderController
 * 
 * @author erwin
 * 
 */
public class CatalogueLoaderPlugin extends IframePlugin
{
	private static final long serialVersionUID = 1L;

	public CatalogueLoaderPlugin(String name, ScreenController<?> parent)
	{
		super(name, parent);

	}

	@Override
	public String getIframeSrc()
	{
		return CatalogLoaderController.BASE_URL + CatalogLoaderController.LOAD_LIST_URI;
	}

}
