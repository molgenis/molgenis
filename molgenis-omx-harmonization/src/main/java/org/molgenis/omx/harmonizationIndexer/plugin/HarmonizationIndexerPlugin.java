package org.molgenis.omx.harmonizationIndexer.plugin;

import org.molgenis.framework.ui.IframePlugin;
import org.molgenis.framework.ui.ScreenController;

public class HarmonizationIndexerPlugin extends IframePlugin
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public HarmonizationIndexerPlugin(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}

	@Override
	public String getIframeSrc()
	{
		return "/plugin/ontologyindexer";
	}
}
