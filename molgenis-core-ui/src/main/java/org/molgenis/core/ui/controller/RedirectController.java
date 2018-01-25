package org.molgenis.core.ui.controller;

import org.molgenis.web.PluginController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Plugin that redirects the user to another url.
 * <p>
 * Can be used to create a menu item to show a page outside molgenis
 * <p>
 * Usage: /plugin/redirect?url=http://www.mysite.nl
 */
@Controller
@RequestMapping(RedirectController.URI)
public class RedirectController extends PluginController
{
	public static final String ID = "redirect";
	public static final String URI = PluginController.PLUGIN_URI_PREFIX + ID;

	public RedirectController()
	{
		super(URI);
	}

	@GetMapping
	public View redirect(@RequestParam("url") String url)
	{
		return new RedirectView(url, false, false, false);
	}
}
