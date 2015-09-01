package org.molgenis.ui.controller;

import org.molgenis.ui.MolgenisPluginController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Plugin that redirects the user to another url.
 * 
 * Can be used to create a menu item to show a page outside molgenis
 * 
 * Usage: /plugin/redirect?url=http://www.mysite.nl
 * 
 */
@Controller
@RequestMapping(RedirectController.URI)
public class RedirectController extends MolgenisPluginController
{
	public static final String ID = "redirect";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	public RedirectController()
	{
		super(URI);
	}

	@RequestMapping(method = RequestMethod.GET)
	public View redirect(@RequestParam("url") String url)
	{
		return new RedirectView(url, false, false, false);
	}
}
