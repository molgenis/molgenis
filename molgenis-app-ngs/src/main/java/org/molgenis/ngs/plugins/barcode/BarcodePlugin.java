package org.molgenis.ngs.plugins.barcode;

import org.molgenis.ngs.controller.BarCodeController;
/**
 *
 */
import org.molgenis.framework.ui.IframePlugin;
import org.molgenis.framework.ui.ScreenController;

public class BarcodePlugin extends IframePlugin
{
	private static final long serialVersionUID = 1L;

	public BarcodePlugin(String name, ScreenController<?> parent)
	{
		super(name, parent);

		getModel().setLabel("Barcode");

	}

	@Override
	public String getIframeSrc()
	{
		return BarCodeController.URI;
	}
}