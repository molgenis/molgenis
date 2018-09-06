package org.molgenis.app.manager.controller;

import static java.net.URLConnection.guessContentTypeFromName;
import static java.util.Objects.requireNonNull;
import static org.molgenis.app.manager.controller.AppController.URI;
import static org.molgenis.data.plugin.model.PluginPermission.VIEW_PLUGIN;
import static org.molgenis.web.bootstrap.PluginPopulator.APP_PREFIX;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.molgenis.app.manager.exception.AppIsInactiveException;
import org.molgenis.app.manager.model.AppResponse;
import org.molgenis.app.manager.service.AppManagerService;
import org.molgenis.core.ui.menu.MenuReaderService;
import org.molgenis.data.file.FileStore;
import org.molgenis.data.plugin.model.PluginIdentity;
import org.molgenis.data.plugin.model.PluginPermissionDeniedException;
import org.molgenis.security.core.UserPermissionEvaluator;
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

@Controller
@RequestMapping(URI)
public class AppController extends PluginController {
  public static final String ID = "app";
  public static final String URI = PluginController.PLUGIN_URI_PREFIX + ID;

  private final FileStore fileStore;
  private final AppManagerService appManagerService;
  private final UserPermissionEvaluator userPermissionEvaluator;
  private final AppSettings appSettings;
  private final MenuReaderService menuReaderService;

  public AppController(
      AppManagerService appManagerService,
      UserPermissionEvaluator userPermissionEvaluator,
      AppSettings appSettings,
      MenuReaderService menuReaderService,
      FileStore fileStore) {
    super(URI);
    this.appManagerService = requireNonNull(appManagerService);
    this.userPermissionEvaluator = requireNonNull(userPermissionEvaluator);
    this.appSettings = requireNonNull(appSettings);
    this.menuReaderService = requireNonNull(menuReaderService);
    this.fileStore = requireNonNull(fileStore);
  }

  @GetMapping("/{appName}/**")
  @Nullable
  public ModelAndView serveApp(
      @PathVariable String appName,
      Model model,
      HttpServletRequest request,
      HttpServletResponse response)
      throws IOException {
    PluginIdentity pluginIdentity = new PluginIdentity(APP_PREFIX + appName);
    if (!userPermissionEvaluator.hasPermission(pluginIdentity, VIEW_PLUGIN)) {
      throw new PluginPermissionDeniedException(appName, VIEW_PLUGIN);
    }

    String wildCardPath = extractWildcardPath(request, appName);
    if (wildCardPath.isEmpty()) {
      RedirectView redirectView = new RedirectView(findAppMenuURL(appName));
      redirectView.setExposePathVariables(false);
      return new ModelAndView(redirectView);
    }

    AppResponse appResponse = appManagerService.getAppByName(appName);

    if (!appResponse.getIsActive()) {
      throw new AppIsInactiveException(appName);
    } else if (isResourceRequest(wildCardPath)) {
      // Copies resource to response and returns null to short circuit the template filter
      serveAppResource(response, wildCardPath, appResponse);
      return null;
    } else {
      return serveAppTemplate(appName, model, appResponse);
    }
  }

  private static boolean isResourceRequest(String wildCardPath) {
    return wildCardPath.startsWith("/js/")
        || wildCardPath.startsWith("/css/")
        || wildCardPath.startsWith("/img/");
  }

  private ModelAndView serveAppTemplate(String appName, Model model, AppResponse appResponse) {
    model.addAttribute("baseUrl", findAppMenuURL(appName));
    model.addAttribute("template", appResponse.getTemplateContent());
    model.addAttribute("lng", LocaleContextHolder.getLocale().getLanguage());
    model.addAttribute("fallbackLng", appSettings.getLanguageCode());
    model.addAttribute("app", appResponse);

    return new ModelAndView("view-app");
  }

  private void serveAppResource(
      HttpServletResponse response, String wildCardPath, AppResponse appResponse)
      throws IOException {
    File requestedResource = fileStore.getFile(appResponse.getResourceFolder() + wildCardPath);
    response.setContentType(guessMimeType(requestedResource.getName()));
    response.setContentLength((int) requestedResource.length());
    response.setHeader(
        CONTENT_DISPOSITION,
        "attachment; filename=" + requestedResource.getName().replace(" ", "_"));

    try (InputStream is = new FileInputStream(requestedResource)) {
      FileCopyUtils.copy(is, response.getOutputStream());
    }
  }

  private static String extractWildcardPath(HttpServletRequest request, String key) {
    int index = request.getRequestURI().indexOf(key);
    return request.getRequestURI().substring(index + key.length());
  }

  private String findAppMenuURL(String appName) {
    return menuReaderService.getMenu().findMenuItemPath(APP_PREFIX + appName);
  }

  private static String guessMimeType(String fileName) {
    if (fileName.endsWith(".js")) return "application/javascript;charset=UTF-8";
    if (fileName.endsWith(".css")) return "text/css;charset=UTF-8";
    return guessContentTypeFromName(fileName);
  }
}
