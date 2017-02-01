package org.molgenis.swagger.controller;

import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletResponse;
import java.util.stream.Collectors;

import static org.molgenis.swagger.controller.SwaggerController.URI;

@Controller
@RequestMapping(URI)
public class SwaggerController extends MolgenisPluginController
{

	private static final String ID = "swagger";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	private final MetaDataService metaDataService;

	@Autowired
	public SwaggerController(MetaDataService metaDataService)
	{
		super(URI);
		this.metaDataService = metaDataService;
	}

	@RequestMapping
	public String init()
	{
		String swaggerUrl = ServletUriComponentsBuilder.fromCurrentContextPath().path("/plugin/swagger/swagger.yml")
				.toUriString();
		return "redirect:" + ServletUriComponentsBuilder.fromCurrentContextPath().path("/swagger-ui/index.html")
				.queryParam("url", swaggerUrl).toUriString();
	}

	@RequestMapping(path = "/swagger.yml", produces = "text/yaml")
	public String swagger(Model model, HttpServletResponse response)
	{
		response.setContentType("text/yaml");
		response.setCharacterEncoding("UTF-8");
		model.addAttribute("entityTypes",
				metaDataService.getEntityTypes().filter(e -> !e.isAbstract()).map(EntityType::getName).sorted()
						.collect(Collectors.toList()));
		return "view-swagger";
	}
}





