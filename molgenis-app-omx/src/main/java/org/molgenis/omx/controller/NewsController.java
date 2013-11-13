package org.molgenis.omx.controller;

import static org.molgenis.omx.controller.NewsController.URI;

import org.molgenis.framework.ui.MolgenisPluginController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller that handles news page requests
 */
@Controller
@RequestMapping(URI)
public class NewsController extends AbstractStaticContectController
{
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + "news";
	public static final String UNIQUEREFERENCE = "news";
	
	public NewsController()
	{
		super(UNIQUEREFERENCE, URI);
	}
}
