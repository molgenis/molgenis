package org.molgenis.mappingservice;

import static org.molgenis.mappingservice.MappingServiceController.URI;
import org.molgenis.framework.ui.MolgenisPluginController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping(URI)
public class MappingServiceController extends MolgenisPluginController
{
	public static final String ID = "mappingservice";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	private static final String VIEW_NAME = "view-mappingservice";

	@Autowired
	public MappingServiceController()
	{
		super(URI);
	}

	@RequestMapping
	public String init(@RequestParam(value = "entity", required = false) String selectedEntityName, Model model)
	{
		return VIEW_NAME;
	}
}