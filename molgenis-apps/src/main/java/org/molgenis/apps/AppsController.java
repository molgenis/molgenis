package org.molgenis.apps;

import org.molgenis.apps.model.App;
import org.molgenis.apps.model.AppMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.system.core.FreemarkerTemplate;
import org.molgenis.file.FileStore;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.core.Permission;
import org.molgenis.ui.MolgenisPluginController;
import org.molgenis.util.ErrorMessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.net.URISyntaxException;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static org.molgenis.apps.AppsController.URI;
import static org.molgenis.apps.model.AppMetaData.APP;
import static org.molgenis.ui.FileStoreConstants.FILE_STORE_PLUGIN_APPS_PATH;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@RequestMapping(URI)
public class AppsController extends MolgenisPluginController
{
	private static final Logger LOG = LoggerFactory.getLogger(AppsController.class);

	public static final String ID = "apps";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	private static final String VIEW_NAME = "view-apps";

	private final DataService dataService;
	private final FileStore fileStore;
	private final MolgenisPermissionService permissionService;

	@Autowired
	public AppsController(DataService dataService, FileStore fileStore, MolgenisPermissionService permissionService)
	{
		super(URI);

		this.dataService = requireNonNull(dataService);
		this.fileStore = requireNonNull(fileStore);
		this.permissionService = requireNonNull(permissionService);
	}

	@RequestMapping(method = GET)
	public String init(Model model)
	{
		model.addAttribute("appEntityTypeId", APP);
		model.addAttribute("apps", getApps().map(this::toAppInfoDto).collect(toList()));
		return VIEW_NAME;
	}

	private Stream<App> getApps()
	{
		Query<App> query = dataService.query(APP, App.class);
		query.sort().on(AppMetaData.NAME);
		Stream<App> apps = query.findAll();
		if (!permissionService.hasPermissionOnEntity(APP, Permission.WRITE))
		{
			apps = apps.filter(App::isActive);
		}
		return apps;
	}

	@RequestMapping(value = "/{appId}", method = GET)
	public String viewApp(@PathVariable("appId") String appId, Model model, HttpServletResponse response)
	{
		App app = dataService.findOneById(APP, appId, App.class);
		if (app == null)
		{
			model.addAttribute("errorMessage", format("Unknown app '%s'", appId));
			response.setStatus(SC_BAD_REQUEST);
			return "forward:" + URI;
		}
		if (!app.isActive())
		{
			model.addAttribute("errorMessage", format("App '%s' is deactivated", app.getName()));
			response.setStatus(SC_BAD_REQUEST);
			return "forward:" + URI;
		}

		model.addAttribute("app", toAppInfoDto(app));
		if (app.getUseFreemarkerTemplate())
		{
			FreemarkerTemplate htmlTemplate = app.getHtmlTemplate();
			return htmlTemplate.getNameWithoutExtension();
		}
		else
		{
			return "redirect:/" + FILE_STORE_PLUGIN_APPS_PATH + "/" + app.getId() + "/index.html";
		}
	}

	@Transactional
	@RequestMapping(value = "/{appId}/activate", method = POST)
	@ResponseStatus(OK)
	public void activateApp(@PathVariable("appId") String appId)
	{
		App app = dataService.findOneById(APP, appId, App.class);
		if (app == null)
		{
			throw new AppsException(format("Unknown app '%s'", appId));
		}
		if (app.isActive())
		{
			throw new AppsException(format("App '%s' already activated", app.getName()));
		}

		app.setActive(true);
		dataService.update(APP, app);
	}

	@RequestMapping(value = "/{appId}/deactivate", method = POST)
	@ResponseStatus(OK)
	public void deactivateApp(@PathVariable("appId") String appId, Model model)
	{
		App app = dataService.findOneById(APP, appId, App.class);
		if (app == null)
		{
			throw new AppsException(format("Unknown app '%s'", appId));
		}
		if (!app.isActive())
		{
			throw new AppsException(format("App '%s' already deactivated", app.getName()));
		}

		app.setActive(false);
		dataService.update(APP, app);
	}

	private AppInfoDto toAppInfoDto(App app)
	{
		java.net.URI iconHref;
		String iconHrefStr = app.getIconHref();
		if (iconHrefStr != null)
		{
			try
			{
				iconHref = new java.net.URI(app.getIconHref());
			}
			catch (URISyntaxException e)
			{
				LOG.error("App icon href '{}' is not a valid URI", iconHrefStr);
				throw new RuntimeException("An error occurred while retrieving app");
			}
		}
		else
		{
			iconHref = null;
		}

		return AppInfoDto.builder()
						 .setId(app.getId())
						 .setName(app.getName())
						 .setDescription(app.getDescription())
						 .setActive(app.isActive())
						 .setIconHref(iconHref)
						 .build();
	}

	@ExceptionHandler(AppsException.class)
	@ResponseStatus(BAD_REQUEST)
	@ResponseBody
	public ErrorMessageResponse handleAppsException(AppsException e)
	{
		LOG.warn("Apps exception occurred", e);
		return new ErrorMessageResponse(new ErrorMessageResponse.ErrorMessage(e.getMessage()));
	}

	@ExceptionHandler(RuntimeException.class)
	@ResponseStatus(INTERNAL_SERVER_ERROR)
	@ResponseBody
	public ErrorMessageResponse handleRuntimeException(RuntimeException e)
	{
		LOG.error("Runtime exception occurred.", e);
		return new ErrorMessageResponse(new ErrorMessageResponse.ErrorMessage(e.getMessage()));
	}
}
