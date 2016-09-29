package org.molgenis.apps.controller;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.molgenis.apps.model.App;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.file.FileStore;
import org.molgenis.ui.MolgenisPluginController;
import org.molgenis.ui.menu.MenuReaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static java.io.File.separator;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.apps.controller.AppsController.URI;
import static org.molgenis.apps.model.AppMetaData.APP;
import static org.molgenis.security.core.utils.SecurityUtils.getCurrentUsername;

@Controller
@RequestMapping(URI)
public class AppsController extends MolgenisPluginController
{
	private static final Logger LOG = LoggerFactory.getLogger(AppsController.class);

	public static final String ID = "apps";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	private static final String VIEW_NAME = "view-apps";
	private static final String API_URI = "/api/";

	private DataService dataService;
	private FileStore fileStore;
	private final MenuReaderService menuReaderService;

	@Autowired
	public AppsController(DataService dataService, FileStore fileStore, MenuReaderService menuReaderService)
	{
		super(URI);

		this.dataService = requireNonNull(dataService);
		this.fileStore = requireNonNull(fileStore);
		this.menuReaderService = menuReaderService;
	}

	@RequestMapping
	public String init(Model model)
	{
		List<Entity> apps = dataService.findAll(APP).collect(toList());
		model.addAttribute("apps", apps);
		return VIEW_NAME;
	}

	@RequestMapping(value = { "/{appName}" })
	public String viewApp(@PathVariable String appName, Model model, HttpServletRequest request) throws IOException
	{
		Entity appEntity = dataService.findOneById(APP, appName);
		model.addAttribute("username", getCurrentUsername());
		model.addAttribute("apiUrl", getApiUrl(request));
		model.addAttribute("baseUrl", getBaseUrl(appName));
		if (appEntity == null)
		{
			model.addAttribute("appNotAvailableMessage", appName);
			return VIEW_NAME;
		}
		else
		{
			return "view-" + appName;
		}
	}

	@RequestMapping(value = "/{appName}/activate")
	@ResponseBody
	public void activateApp(@PathVariable String appName) throws ZipException
	{
		App app = dataService.findOneById(APP, appName, App.class);
		app.setActive(true);

		File fileStoreFile = fileStore.getFile(app.getSourceFiles().getId());
		if (fileStoreFile != null)
		{
			ZipFile zipFile = new ZipFile(fileStoreFile);
			zipFile.extractAll(fileStore.getStorageDir() + separator + "appstore" + separator + appName + separator);

			dataService.update(APP, app);
		}
		else
		{
			// TODO Send message?
		}
	}

	@RequestMapping(value = "/{appName}/deactivate")
	@ResponseBody
	public void deactivateApp(@PathVariable String appName) throws IOException
	{
		App app = dataService.findOneById(APP, appName, App.class);
		app.setActive(false);

		fileStore.deleteDirectory("appstore" + separator + appName);
		dataService.update(APP, app);
	}

	private static String getApiUrl(HttpServletRequest request)
	{
		String apiUrl;
		if (StringUtils.isEmpty(request.getHeader("X-Forwarded-Host")))
		{
			apiUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getLocalPort() + API_URI;
		}
		else
		{
			apiUrl = request.getScheme() + "://" + request.getHeader("X-Forwarded-Host") + API_URI;
		}
		return apiUrl;
	}

	private String getBaseUrl(String appName)
	{
		return menuReaderService.getMenu().findMenuItemPath(ID) + "/" + appName;
	}
}
