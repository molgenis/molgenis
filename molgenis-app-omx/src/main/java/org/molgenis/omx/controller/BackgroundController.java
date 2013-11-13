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
public class BackgroundController extends AbstractStaticContectController
{
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + "background";
	public static final String UNIQUEREFERENCE = "background";
	
	public BackgroundController()
	{
		super(UNIQUEREFERENCE, URI);
	}
}