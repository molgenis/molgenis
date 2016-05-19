package org.molgenis.ui.goadmanager;

import static org.molgenis.ui.goadmanager.GoadManagerController.URI;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import org.molgenis.ui.MolgenisPluginController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(URI)
public class GoadManagerController extends MolgenisPluginController
{
	public static final String ID = "goadmanager";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	public GoadManagerController() {
		super(URI);
	}
	
	@RequestMapping(method = GET)
	public String init()
	{
		return "view-goadmanager";
	}
}
