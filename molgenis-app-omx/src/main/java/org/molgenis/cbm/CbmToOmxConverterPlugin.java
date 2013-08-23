package org.molgenis.cbm;

import javax.servlet.annotation.MultipartConfig;

import org.molgenis.framework.ui.IframePlugin;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.omx.controller.CbmToOmxConverterController;

@MultipartConfig
public class CbmToOmxConverterPlugin extends IframePlugin
{
	private static final long serialVersionUID = 1L;

	public CbmToOmxConverterPlugin(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}

	@Override
	public String getIframeSrc()
	{
		return CbmToOmxConverterController.URI;
	}
}