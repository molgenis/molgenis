package org.molgenis.swagger.controller;

import org.molgenis.data.DataService;
import org.molgenis.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.molgenis.swagger.controller.SwaggerController.URI;

@Controller
@RequestMapping(URI)
public class SwaggerController extends MolgenisPluginController
{

	private static final String ID = "swagger";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	private final DataService dataService;

	@Autowired
	public SwaggerController(DataService dataService)
	{
		super(URI);
		this.dataService = dataService;
	}

	@RequestMapping
	public String init()
	{
		// TODO do not hardcode localhost
		return "redirect:http://localhost:8080/swagger-ui/index.html?url=http://localhost:8080/menu/main/swagger/swagger.yml";
	}

	@RequestMapping(path = "/swagger.yml", produces = "text/x-yaml")
	public String swagger(Model model)
	{
		// serveert onze mooie yml
		return "view-swagger";
	}
}





