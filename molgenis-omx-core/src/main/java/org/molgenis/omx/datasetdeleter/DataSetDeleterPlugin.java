package org.molgenis.omx.datasetdeleter;

import org.molgenis.framework.ui.IframePlugin;
import org.molgenis.framework.ui.ScreenController;

public class DataSetDeleterPlugin extends IframePlugin
{
	private static final long serialVersionUID = 1L;

	public DataSetDeleterPlugin(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}

	@Override
	public String getIframeSrc()
	{
		return DataSetDeleterController.URI;
	}

}
