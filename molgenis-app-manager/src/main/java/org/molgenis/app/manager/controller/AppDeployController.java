package org.molgenis.app.manager.controller;

import org.molgenis.app.manager.exception.AppManagerException;
import org.molgenis.app.manager.model.AppResponse;
import org.molgenis.app.manager.service.AppManagerService;
import org.molgenis.web.PluginController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import static java.util.Objects.requireNonNull;
import static org.molgenis.app.manager.controller.AppDeployController.URI;

@Controller
@RequestMapping(URI)
public class AppDeployController extends PluginController
{
	public static final String ID = "app";
	public static final String URI = PluginController.PLUGIN_URI_PREFIX + ID;

	private AppManagerService appManagerService;

	public AppDeployController(AppManagerService appManagerService)
	{
		super(URI);
		this.appManagerService = requireNonNull(appManagerService);
	}

	@RequestMapping
	public String init()
	{
		return "redirect: " + AppManagerController.URI;
	}

	@RequestMapping("/{id}")
	public String deployApp(@PathVariable(value = "id") String id, Model model)
	{
		AppResponse appResponse = appManagerService.getAppById(id);
		if (!appResponse.getIsActive())
		{
			throw new AppManagerException("Access denied for inactive app [" + id + "]");
		}

		model.addAttribute("app", appResponse);
		return "view-app";
	}
}
