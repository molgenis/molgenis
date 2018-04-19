package org.molgenis.app.manager.controller;

import org.molgenis.app.manager.exception.AppManagerException;
import org.molgenis.app.manager.model.AppResponse;
import org.molgenis.app.manager.service.AppDeployService;
import org.molgenis.app.manager.service.AppManagerService;
import org.molgenis.core.ui.menu.MenuReaderService;
import org.molgenis.settings.AppSettings;
import org.molgenis.web.PluginController;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

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

	private final AppDeployService appDeployService;
	private final AppManagerService appManagerService;
	private final AppSettings appSettings;
	private final MenuReaderService menuReaderService;

	public AppDeployController(AppDeployService appDeployService, AppManagerService appManagerService,
			AppSettings appSettings, MenuReaderService menuReaderService)
	{
		super(URI);
		this.appDeployService = requireNonNull(appDeployService);
		this.appManagerService = requireNonNull(appManagerService);
		this.appSettings = requireNonNull(appSettings);
		this.menuReaderService = requireNonNull(menuReaderService);
	}

	@RequestMapping("/{uri}/{version}/**")
	public String deployApp(@PathVariable String uri, @PathVariable String version, Model model)
	{
		AppResponse appResponse = appManagerService.getAppByUri(uri + "/" + version);
		if (!appResponse.getIsActive())
		{
			throw new AppManagerException("Access denied for inactive app at location [/app/" + uri + "]");
		}

		String baseUrl = menuReaderService.getMenu().findMenuItemPath("app/" + uri + "/" + version + "/");
		model.addAttribute("baseUrl", baseUrl);

		String template = appDeployService.configureTemplateResourceReferencing(appResponse.getTemplateContent(),
				baseUrl);
		model.addAttribute("template", template);
		model.addAttribute("lng", LocaleContextHolder.getLocale().getLanguage());
		model.addAttribute("fallbackLng", appSettings.getLanguageCode());
		model.addAttribute("app", appResponse);

		return "view-app";
	}

	@ResponseStatus(HttpStatus.OK)
	@RequestMapping("/{uri}/{version}/js/{fileName:.+}")
	public void loadJavascriptResources(@PathVariable String uri, @PathVariable String version,
			@PathVariable String fileName, HttpServletResponse response) throws IOException
	{
		appDeployService.loadJavascriptResources(uri + "/" + version, fileName, response);
	}

	@ResponseStatus(HttpStatus.OK)
	@RequestMapping("/{uri}/{version}/css/{fileName:.+}")
	public void loadCSSResources(@PathVariable String uri, @PathVariable String version, @PathVariable String fileName,
			HttpServletResponse response) throws IOException
	{
		appDeployService.loadCSSResources(uri + "/" + version, fileName, response);
	}

	@ResponseStatus(HttpStatus.OK)
	@RequestMapping("/{uri}/{version}/img/{fileName:.+}")
	public void loadImageResources(@PathVariable String uri, @PathVariable String version,
			@PathVariable String fileName, HttpServletResponse response) throws IOException
	{
		appDeployService.loadImageResources(uri + "/" + version, fileName, response);
	}
}
