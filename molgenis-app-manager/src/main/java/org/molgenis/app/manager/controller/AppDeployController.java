package org.molgenis.app.manager.controller;

import org.molgenis.app.manager.exception.AppManagerException;
import org.molgenis.app.manager.model.AppResponse;
import org.molgenis.app.manager.service.AppDeployService;
import org.molgenis.app.manager.service.AppManagerService;
import org.molgenis.core.ui.menu.MenuReaderService;
import org.molgenis.settings.AppSettings;
import org.molgenis.web.PluginController;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.annotation.Order;
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
	private AppSettings appSettings;
	private MenuReaderService menuReaderService;

	public AppDeployController(AppDeployService appDeployService, AppManagerService appManagerService,
			AppSettings appSettings, MenuReaderService menuReaderService)
	{
		super(URI);
		this.appDeployService = requireNonNull(appDeployService);
		this.appManagerService = requireNonNull(appManagerService);
		this.appSettings = requireNonNull(appSettings);
		this.menuReaderService = requireNonNull(menuReaderService);
	}

	@RequestMapping
	public String init()
	{
		return "redirect: " + AppManagerController.URI;
	}

	@Order // Set to lowest order to prevent resource requests being handled by this mapping
	@RequestMapping("/{uri}/**")
	public String deployApp(@PathVariable String uri, Model model)
	{
		AppResponse appResponse = appManagerService.getAppByUri(uri);
		if (!appResponse.getIsActive())
		{
			throw new AppManagerException("Access denied for inactive app at location [/app/" + uri + "]");
		}
		model.addAttribute("app", appResponse);

		model.addAttribute("baseUrl", menuReaderService.getMenu().findMenuItemPath("app/" + uri + "/"));
		model.addAttribute("lng", LocaleContextHolder.getLocale().getLanguage());
		model.addAttribute("fallbackLng", appSettings.getLanguageCode());

		return "view-app";
	}

	@RequestMapping("/{uri}/js/{fileName:.+}")
	public void loadJavascriptResources(@PathVariable String uri, @PathVariable String fileName,
			HttpServletResponse response) throws IOException
	{
		appDeployService.loadJavascriptResources(uri, fileName, response);
	}

	@RequestMapping("/{uri}/css/{fileName:.+}")
	public void loadCSSResources(@PathVariable String uri, @PathVariable String fileName, HttpServletResponse response)
			throws IOException
	{
		appDeployService.loadCSSResources(uri, fileName, response);
	}

	@RequestMapping("/{uri}/img/{fileName:.+}")
	public void loadImageResources(@PathVariable String uri, @PathVariable String fileName,
			HttpServletResponse response) throws IOException
	{
		appDeployService.loadImageResources(uri, fileName, response);
	}
}
