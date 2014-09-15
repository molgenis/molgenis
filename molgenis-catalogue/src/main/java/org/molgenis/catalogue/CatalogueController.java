package org.molgenis.catalogue;

import static org.molgenis.catalogue.CatalogueController.URI;

import org.molgenis.framework.ui.MolgenisPluginController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping(URI)
public class CatalogueController extends MolgenisPluginController
{
	public static final String ID = "catalogue";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	private static final String VIEW_NAME = "view-catalogue";

	public CatalogueController()
	{
		super(URI);
	}

	@RequestMapping
	public String showView(@RequestParam(value = "entity", required = false) String entity, Model model)
	{
		model.addAttribute("entity", entity);
		return VIEW_NAME;
	}
}
