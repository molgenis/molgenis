package org.molgenis.ui;

import org.springframework.beans.factory.annotation.Autowired;
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

	private final MolgenisUi molgenisUi;

	@Autowired
	public MolgenisRootController(MolgenisUi molgenisUi)
	{
		if (molgenisUi == null) throw new IllegalArgumentException("molgenisUi is null");
		this.molgenisUi = molgenisUi;
	}

	@RequestMapping(method =
	{ RequestMethod.GET, RequestMethod.POST })
	public String index()
	{
		if (USE_SPRING_UI)
		{
			MolgenisUiMenuItem activeItem = molgenisUi.getMenu().getActiveItem();
			if (activeItem != null) return "forward:" + MolgenisPluginController.PLUGIN_URI_PREFIX + activeItem.getId();
			else throw new RuntimeException(
					"Warning! Menu does not contain any (accessible) items. Check your UI definition and permissions.");
		}
		else
		{
			return "redirect:molgenis.do?__target=main&select=Home";
		}
	}
}
