package org.molgenis.app.manager.controller;

import org.molgenis.app.manager.exception.AppIsInactiveException;
import org.molgenis.app.manager.model.AppResponse;
import org.molgenis.app.manager.service.AppDeployService;
import org.molgenis.app.manager.service.AppManagerService;
import org.molgenis.core.ui.menu.MenuReaderService;
import org.molgenis.settings.AppSettings;
import org.molgenis.web.PluginController;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
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

	@GetMapping("/{uri}/**")
	public ModelAndView serveApp(@PathVariable String uri, Model model, HttpServletRequest request,
			HttpServletResponse response) throws IOException
	{
		String wildCardPath = extractWildcardPath(request, uri);
		if (wildCardPath.isEmpty())
		{
			RedirectView redirectView = new RedirectView(findAppMenuURL(uri));
			redirectView.setExposePathVariables(false);
			return new ModelAndView(redirectView);
		}

		AppResponse appResponse = appManagerService.getAppByUri(uri);
		if (wildCardPath.startsWith("/js/") || wildCardPath.startsWith("/css/") || wildCardPath.startsWith("/img/"))
		{
			// Load resource into the response and return null
			appDeployService.loadResource(appResponse.getResourceFolder() + wildCardPath, response);
			return null;
		}

		if (!appResponse.getIsActive())
		{
			throw new AppIsInactiveException(uri);
		}

		model.addAttribute("baseUrl", findAppMenuURL(uri));
		model.addAttribute("template", appResponse.getTemplateContent());
		model.addAttribute("lng", LocaleContextHolder.getLocale().getLanguage());
		model.addAttribute("fallbackLng", appSettings.getLanguageCode());
		model.addAttribute("app", appResponse);

		return new ModelAndView("view-app");
	}

	private static String extractWildcardPath(HttpServletRequest request, String key)
	{
		int index = request.getRequestURI().indexOf(key);
		return request.getRequestURI().substring(index + key.length());
	}

	private String findAppMenuURL(String uri)
	{
		return menuReaderService.getMenu().findMenuItemPath("app/" + uri + "/");
	}
}
