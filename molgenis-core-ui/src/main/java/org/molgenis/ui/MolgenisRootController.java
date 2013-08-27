package org.molgenis.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Redirects '/' to the active plugin in the main menu
 */
@Controller
@RequestMapping("/")
public class MolgenisRootController
{
	// TODO remove flag after removing molgenis UI framework
	public static final boolean USE_SPRING_UI = false;

	@RequestMapping(method =
	{ RequestMethod.GET, RequestMethod.POST })
	public String index()
	{
		if (USE_SPRING_UI) return "forward:" + MolgenisMenuController.URI;
		else return "redirect:molgenis.do?__target=main&select=Home";
	}
}
