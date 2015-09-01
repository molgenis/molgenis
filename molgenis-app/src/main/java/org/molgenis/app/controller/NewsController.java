package org.molgenis.app.controller;

import static org.molgenis.app.controller.NewsController.URI;

import org.molgenis.ui.MolgenisPluginController;
import org.molgenis.ui.controller.AbstractStaticContentController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller that handles news page requests
 */
@Controller
@RequestMapping(URI)
public class NewsController extends AbstractStaticContentController
{
	public static final String ID = "news";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	
	public NewsController()
	{
		super(ID, URI);
	}
}
