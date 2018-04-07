package org.molgenis.apps;

import org.molgenis.app.manager.meta.App;
import org.molgenis.app.manager.meta.AppMetadata;
import org.molgenis.core.ui.data.system.core.FreemarkerTemplate;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.EntityTypePermission;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.web.ErrorMessageResponse;
import org.molgenis.web.PluginController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static org.molgenis.app.manager.meta.AppMetadata.APP;
import static org.molgenis.core.ui.FileStoreConstants.FILE_STORE_PLUGIN_APPS_PATH;
import static org.springframework.http.HttpStatus.*;

public class AppsController extends PluginController
{
	private static final Logger LOG = LoggerFactory.getLogger(AppsController.class);

	public static final String ID = "apps";
	public static final String URI = PluginController.PLUGIN_URI_PREFIX + ID;

	private static final String VIEW_NAME = "view-apps";

	private final DataService dataService;
	private final UserPermissionEvaluator permissionService;

	public AppsController(DataService dataService, UserPermissionEvaluator permissionService)
	{
		super(URI);

		this.dataService = requireNonNull(dataService);
		this.permissionService = requireNonNull(permissionService);
	}

	@GetMapping
	public String init(Model model)
	{
		model.addAttribute("appEntityTypeId", APP);
		model.addAttribute("apps", getApps().map(this::toAppInfoDto).collect(toList()));
		return VIEW_NAME;
	}

	private Stream<App> getApps()
	{
		Query<App> query = dataService.query(APP, App.class);
		query.sort().on(AppMetadata.LABEL);
		Stream<App> apps = query.findAll();
		if (!permissionService.hasPermission(new EntityTypeIdentity(APP), EntityTypePermission.WRITE))
		{
			apps = apps.filter(App::isActive);
		}
		return apps;
	}

	@GetMapping(value = "/{appId}/**")
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
			model.addAttribute("errorMessage", format("App '%s' is deactivated", app.getLabel()));
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
	@PostMapping("/{appId}/activate")
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
			throw new AppsException(format("App '%s' already activated", app.getLabel()));
		}

		app.setActive(true);
		dataService.update(APP, app);
	}

	@PostMapping("/{appId}/deactivate")
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
			throw new AppsException(format("App '%s' already deactivated", app.getLabel()));
		}

		app.setActive(false);
		dataService.update(APP, app);
	}

	private AppInfoDto toAppInfoDto(App app)
	{
		return AppInfoDto.builder()
						 .setId(app.getId())
						 .setName(app.getLabel())
						 .setDescription(app.getDescription())
						 .setActive(app.isActive())
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
