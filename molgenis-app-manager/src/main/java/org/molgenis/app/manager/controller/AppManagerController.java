package org.molgenis.app.manager.controller;

import net.lingala.zip4j.exception.ZipException;
import org.molgenis.app.manager.model.AppResponse;
import org.molgenis.app.manager.service.AppManagerService;
import org.molgenis.web.PluginController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static java.util.Objects.requireNonNull;

@Controller
@RequestMapping(AppManagerController.URI)
public class AppManagerController extends PluginController
{
	public static final String ID = "appmanager";
	public static final String URI = PluginController.PLUGIN_URI_PREFIX + ID;

	private final AppManagerService appManagerService;

	public AppManagerController(AppManagerService appManagerService)
	{
		super(URI);
		this.appManagerService = requireNonNull(appManagerService);
	}

	@GetMapping
	public String init()
	{
		return "view-app-manager";
	}

	@ResponseBody
	@GetMapping("/apps")
	public List<AppResponse> getApps()
	{
		return appManagerService.getApps();
	}

	@ResponseStatus(HttpStatus.OK)
	@GetMapping("/activate/{id}")
	public void activateApp(@PathVariable(value = "id") String id)
	{
		appManagerService.activateApp(id);
	}

	@ResponseStatus(HttpStatus.OK)
	@GetMapping("/deactivate/{id}")
	public void deactivateApp(@PathVariable(value = "id") String id)
	{
		appManagerService.deactivateApp(id);
	}

	@ResponseStatus(HttpStatus.OK)
	@GetMapping("/delete/{id}")
	public void deleteApp(@PathVariable("id") String id) throws IOException
	{
		appManagerService.deleteApp(id);
	}

	@ResponseStatus(HttpStatus.OK)
	@PostMapping("/upload")
	public void uploadApp(@RequestParam("file") MultipartFile multipartFile) throws IOException, ZipException
	{
		appManagerService.uploadApp(multipartFile);
	}
}
