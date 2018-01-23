package org.molgenis.core.ui;

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
	@RequestMapping(method = { RequestMethod.GET, RequestMethod.POST })
	public String index()
	{
		return "forward:" + MolgenisMenuController.URI;
	}
}
