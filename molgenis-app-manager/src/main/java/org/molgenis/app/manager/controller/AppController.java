package org.molgenis.app.manager.controller;

import org.molgenis.app.manager.exception.AppIsInactiveException;
import org.molgenis.app.manager.model.AppResponse;
import org.molgenis.app.manager.service.AppManagerService;
import org.molgenis.core.ui.menu.MenuReaderService;
import org.molgenis.settings.AppSettings;
import org.molgenis.web.PluginController;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static java.net.URLConnection.guessContentTypeFromName;
import static java.util.Objects.requireNonNull;
import static org.molgenis.app.manager.controller.AppController.URI;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;

@Controller
@RequestMapping(URI)
public class AppController extends PluginController
{
	public static final String ID = "app";
	public static final String URI = PluginController.PLUGIN_URI_PREFIX + ID;

	private final AppManagerService appManagerService;
	private final AppSettings appSettings;
	private final MenuReaderService menuReaderService;

	public AppController(AppManagerService appManagerService,
			AppSettings appSettings, MenuReaderService menuReaderService)
	{
		super(URI);
		this.appManagerService = requireNonNull(appManagerService);
		this.appSettings = requireNonNull(appSettings);
		this.menuReaderService = requireNonNull(menuReaderService);
	}

	@GetMapping("/{uri}/**")
	@Nullable
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

		if (!appResponse.getIsActive())
		{
			throw new AppIsInactiveException(uri);
		}
		else if (isResourceRequest(wildCardPath))
		{
			// Copies resource to response and returns null to short circuit the template filter
			serveAppResource(response, wildCardPath, appResponse);
			return null;
		}
		else
		{
			return serveAppTemplate(uri, model, appResponse);
		}
	}

	private static boolean isResourceRequest(String wildCardPath)
	{
		return wildCardPath.startsWith("/js/") || wildCardPath.startsWith("/css/") || wildCardPath.startsWith(
				"/img/");
	}

	private ModelAndView serveAppTemplate(@PathVariable String uri, Model model, AppResponse appResponse)
	{
		model.addAttribute("baseUrl", findAppMenuURL(uri));
		model.addAttribute("template", appResponse.getTemplateContent());
		model.addAttribute("lng", LocaleContextHolder.getLocale().getLanguage());
		model.addAttribute("fallbackLng", appSettings.getLanguageCode());
		model.addAttribute("app", appResponse);

		return new ModelAndView("view-app");
	}

	private void serveAppResource(HttpServletResponse response, String wildCardPath, AppResponse appResponse)
			throws IOException
	{
		File requestedResource = new File(appResponse.getResourceFolder() + wildCardPath);
		response.setContentType(guessMimeType(requestedResource.getName()));
		response.setContentLength((int) requestedResource.length());
		response.setHeader(CONTENT_DISPOSITION,
				"attachment; filename=" + requestedResource.getName().replace(" ", "_"));

		try (InputStream is = new FileInputStream(requestedResource))
		{
			FileCopyUtils.copy(is, response.getOutputStream());
		}
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

	private static String guessMimeType(String fileName)
	{
		if (fileName.endsWith(".js")) return "application/javascript;charset=UTF-8";
		if (fileName.endsWith(".css")) return "text/css;charset=UTF-8";
		return guessContentTypeFromName(fileName);
	}
}
