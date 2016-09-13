package org.molgenis.apps.controller;

import org.molgenis.apps.model.App;
import org.molgenis.apps.model.AppMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.file.FileStore;
import org.molgenis.ui.MolgenisPluginController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import static java.io.File.separator;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.apps.controller.AppsController.URI;
import static org.molgenis.apps.model.AppMetaData.APP;

@Controller
@RequestMapping(URI)
public class AppsController extends MolgenisPluginController
{

	public static final String ID = "apps";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	private static final String VIEW_NAME = "view-apps";

	private DataService dataService;
	private FileStore fileStore;

	@Autowired
	public AppsController(DataService dataService, FileStore fileStore)
	{
		super(URI);
		this.dataService = requireNonNull(dataService);
		this.fileStore = requireNonNull(fileStore);
	}

	@RequestMapping
	public String init(Model model)
	{
		List<Entity> apps = dataService.findAll(APP).collect(toList());
		model.addAttribute("apps", apps);
		return VIEW_NAME;
	}

	@RequestMapping("/create-app")
	public void createApp(@RequestParam(value = "app-name") String appName,
			@RequestParam(value = "app-url") String appUrl, @RequestParam(value = "app-sources") String sources,
			@RequestParam(value = "active") boolean active) throws IOException
	{
		fileStore.createDirectory(appName);
		String appDirectory = fileStore.getStorageDir() + separator + appName;

		App app = new App(new AppMetaData());
		app.setAppName(appName);
		app.setAppUrl(appUrl);
		app.setSourcesDirectory(appDirectory);

		if (sources != null)
		{
			fileStore.store(new FileInputStream(sources), sources);
		}
		app.setActive(active);
		dataService.add(APP, app);
	}
}
