package org.molgenis.omx.controller;

import static org.molgenis.omx.controller.HomeController.URI;

import org.molgenis.framework.ui.MolgenisPluginController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller that handles home page requests
 */
@Controller
@RequestMapping(URI)
public class HomeController extends AbstractStaticContectController
{
	public static final String ID = "home";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	
	public HomeController()
	{
		super(ID, URI);
	}
}
