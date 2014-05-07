package org.molgenis.omx.controller;

import static org.molgenis.omx.controller.HomeController.URI;

import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.ui.controller.AbstractStaticContentController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller that handles home page requests
 */
@Controller
@RequestMapping(URI)
public class HomeController extends AbstractStaticContentController
{
	public static final String ID = "home";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	
	public HomeController()
	{
		super(ID, URI);
	}
}
