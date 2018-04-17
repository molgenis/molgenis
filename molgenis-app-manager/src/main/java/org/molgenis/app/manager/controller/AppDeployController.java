package org.molgenis.app.manager.controller;

import org.molgenis.app.manager.exception.AppManagerException;
import org.molgenis.app.manager.model.AppResponse;
import org.molgenis.app.manager.service.AppDeployService;
import org.molgenis.app.manager.service.AppManagerService;
import org.molgenis.web.PluginController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static java.util.Objects.requireNonNull;
import static org.molgenis.app.manager.controller.AppDeployController.URI;

@Controller
@RequestMapping(URI)
public class AppDeployController extends PluginController
{
	public static final String ID = "app";
	public static final String URI = PluginController.PLUGIN_URI_PREFIX + ID;

	private AppDeployService appDeployService;
	private AppManagerService appManagerService;

	public AppDeployController(AppDeployService appDeployService, AppManagerService appManagerService)
	{
		super(URI);
		this.appDeployService = requireNonNull(appDeployService);
		this.appManagerService = requireNonNull(appManagerService);
	}

	@RequestMapping
	public String init()
	{
		return "redirect: " + AppManagerController.URI;
	}

	@RequestMapping("/{uri}")
	public String deployApp(@PathVariable String uri, Model model)
	{
		AppResponse appResponse = appManagerService.getAppByUri(uri);
		if (!appResponse.getIsActive())
		{
			throw new AppManagerException("Access denied for inactive app at location [/app/" + uri + "]");
		}

		model.addAttribute("app", appResponse);
		return "view-app";
	}

	@RequestMapping("/{uri}/{version}")
	public String deployAppWithSpecificVersion(@PathVariable String uri, @PathVariable String version, Model model)
	{
		AppResponse appResponse = appManagerService.getAppByUriAndVersion(uri, version);
		if (!appResponse.getIsActive())
		{
			throw new AppManagerException(
					"Access denied for inactive app at location [/app/" + uri + "/" + version + "]");
		}

		model.addAttribute("app", appResponse);
		return "view-app";
	}

	@RequestMapping("/{uri}/js/{fileName}")
	public void loadJavascriptResources(@PathVariable String uri, @PathVariable String fileName,
			HttpServletResponse response) throws IOException
	{
		appDeployService.loadJavascriptResources(uri, fileName + ".js", response);
	}

	@RequestMapping("/{uri}/css/**")
	public void loadCSSResources(@PathVariable String uri, HttpServletResponse response) throws IOException
	{
		appDeployService.loadCSSResources(uri, response);
	}
}
