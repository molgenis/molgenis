package org.molgenis.omx.controller;

import static org.molgenis.omx.controller.BackgroundController.URI;

import org.molgenis.framework.ui.MolgenisPluginController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller that handles contact page requests
 */
@Controller
@RequestMapping(URI)
public class BackgroundController extends AbstractStaticContentController
{
	public static final String ID = "background";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	
	public BackgroundController()
	{
		super(ID, URI);
	}
}
