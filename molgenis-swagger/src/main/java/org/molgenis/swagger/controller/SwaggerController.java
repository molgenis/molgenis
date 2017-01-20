package org.molgenis.swagger.controller;

import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.security.core.token.TokenService;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletResponse;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.molgenis.swagger.controller.SwaggerController.URI;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * Serves Swagger documentation of the REST API.
 * @see  <a href="http://swagger.io/">http://swagger.io/</a>
 */
@Controller
@RequestMapping(URI)
public class SwaggerController extends MolgenisPluginController
{

	private static final String ID = "swagger";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	private final MetaDataService metaDataService;
	private final TokenService tokenService;

	@Autowired
	public SwaggerController(MetaDataService metaDataService, TokenService tokenService)
	{
		super(URI);
		this.metaDataService = requireNonNull(metaDataService);
		this.tokenService = requireNonNull(tokenService);
	}

	/**
	 * Serves the Swagger UI which allows you to try out the documented endpoints.
	 * Sets the url parameter to the swagger yaml that describes the REST API.
	 * Creates an apiKey token for the current user.
	 */
	@RequestMapping
	public String init(Model model)
	{
		model.addAttribute("url",
				ServletUriComponentsBuilder.fromCurrentContextPath().path("/plugin/swagger/swagger.yml").toUriString());
		final String currentUsername = SecurityUtils.getCurrentUsername();
		if (currentUsername != null)
		{
			model.addAttribute("token", tokenService.generateAndStoreToken(currentUsername, "For Swagger UI"));
		}
		return "view-swagger-ui";
	}

	/**
	 * Serves the Swagger description of the REST API.
	 * As host, fills in the host where the controller lives.
	 * As options for the entity names, contains only those entity names that the user can actually see.
	 */
	@RequestMapping(path = "/swagger.yml", produces = "text/yaml", method = GET)
	public String swagger(Model model, HttpServletResponse response)
	{
		response.setContentType("text/yaml");
		response.setCharacterEncoding("UTF-8");
		final ServletUriComponentsBuilder servletUriComponentsBuilder = ServletUriComponentsBuilder.fromCurrentContextPath();
		model.addAttribute("scheme", servletUriComponentsBuilder.build().getScheme());
		model.addAttribute("host", servletUriComponentsBuilder.replacePath(null).scheme(null).toUriString().substring(2));
		model.addAttribute("entityTypes",
				metaDataService.getEntityTypes().filter(e -> !e.isAbstract()).map(EntityType::getName).sorted().collect(Collectors.toList()));
		return "view-swagger";
	}
}