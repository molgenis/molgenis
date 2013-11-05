package org.molgenis.omx.controller;

import static org.molgenis.omx.controller.HomeController.URI;

import org.molgenis.framework.server.MolgenisSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Controller that handles home page requests
 */
@Controller
@RequestMapping(URI)
public class HomeController extends ContentController
{
	public static final String URI = "/plugin/home";

	@Autowired
	public HomeController(MolgenisSettings molgenisSettings)
	{
		super(molgenisSettings, URI);
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init(Model model)
	{
		return init("home", model);
	}
}
