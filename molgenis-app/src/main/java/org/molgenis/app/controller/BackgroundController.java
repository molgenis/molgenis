package org.molgenis.app.controller;

import org.molgenis.ui.MolgenisPluginController;
import org.molgenis.ui.controller.AbstractStaticContentController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.molgenis.app.controller.BackgroundController.URI;

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
