package org.molgenis.omx.importer;

import org.molgenis.framework.ui.IframePlugin;
import org.molgenis.framework.ui.ScreenController;

public class ImportWizardPlugin extends IframePlugin
{
	private static final long serialVersionUID = 1L;

	public ImportWizardPlugin(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}

	@Override
	public String getIframeSrc()
	{
		return ImportWizardController.URI;
	}

}
