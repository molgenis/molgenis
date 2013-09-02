package org.molgenis.omx.harmonization.plugins;

import org.molgenis.framework.ui.IframePlugin;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.omx.harmonization.controllers.OntologyAnnotatorController;

public class OntologyAnnotatorPlugin extends IframePlugin
{
	private static final long serialVersionUID = 1L;

	public OntologyAnnotatorPlugin(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}

	@Override
	public String getIframeSrc()
	{
		return OntologyAnnotatorController.URI;
	}
}
