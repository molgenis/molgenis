package org.molgenis.app.manager.controller;

import org.molgenis.app.manager.model.AppRequest;
import org.molgenis.app.manager.model.AppResponse;
import org.molgenis.app.manager.service.AppManagerService;
import org.molgenis.web.PluginController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

import static java.util.Objects.requireNonNull;

@Controller
@RequestMapping(AppManagerController.URI)
public class AppManagerController extends PluginController
{
	public static final String ID = "appmanager";
	public static final String URI = PluginController.PLUGIN_URI_PREFIX + ID;

	private AppManagerService appManagerService;

	public AppManagerController(AppManagerService appManagerService)
	{
		super(URI);
		this.appManagerService = requireNonNull(appManagerService);
	}

	@RequestMapping
	public String init(Model model)
	{
		model.addAttribute("apps", appManagerService.getApps());
		return "view-app-manager";
	}

	@ResponseBody
	@RequestMapping("/apps")
	public List<AppResponse> getApps()
	{
		return appManagerService.getApps();
	}

	@ResponseBody
	@RequestMapping("/create")
	public void createApp(AppRequest appRequest)
	{
		appManagerService.createApp(appRequest);
	}

	@RequestMapping("/edit")
	@ResponseStatus(HttpStatus.OK)
	public void editApp(AppRequest appRequest)
	{
		appManagerService.editApp(appRequest);
	}

	@RequestMapping("/delete")
	@ResponseStatus(HttpStatus.OK)
	public void deleteApp(String id)
	{
		appManagerService.deleteApp(id);
	}
}
