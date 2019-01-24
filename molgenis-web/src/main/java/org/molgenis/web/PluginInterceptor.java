package org.molgenis.web;

import static java.util.Objects.requireNonNull;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.molgenis.data.Entity;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.EntityTypePermission;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.web.menu.MenuReaderService;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/** Interceptor that adds default model objects to all plugin requests that return a view. */
public class PluginInterceptor extends HandlerInterceptorAdapter {
  private final MenuReaderService menuReaderService;
  private final UserPermissionEvaluator permissionService;

  public PluginInterceptor(
      MenuReaderService menuReaderService, UserPermissionEvaluator permissionService) {
    this.menuReaderService = requireNonNull(menuReaderService);
    this.permissionService = requireNonNull(permissionService);
  }

  @Override
  public boolean preHandle(
      HttpServletRequest request, HttpServletResponse response, Object handler) {
    PluginController molgenisPlugin = validateHandler(handler);

    // determine context url for this plugin if no context exists
    String contextUrl = (String) request.getAttribute(PluginAttributes.KEY_CONTEXT_URL);
    if (contextUrl == null) {
      request.setAttribute(PluginAttributes.KEY_CONTEXT_URL, molgenisPlugin.getUri());
    }

    return true;
  }

  @Override
  public void postHandle(
      HttpServletRequest request,
      HttpServletResponse response,
      Object handler,
      ModelAndView modelAndView) {
    if (modelAndView != null) {
      PluginController molgenisPlugin = validateHandler(handler);
      String pluginId = molgenisPlugin.getId();

      // allow controllers that handle multiple plugins to set their plugin id
      if (!modelAndView.getModel().containsKey(PluginAttributes.KEY_PLUGIN_ID)) {
        modelAndView.addObject(PluginAttributes.KEY_PLUGIN_ID, pluginId);
      }

      Entity pluginSettings = molgenisPlugin.getPluginSettings();
      boolean pluginSettingsCanWrite;
      if (pluginSettings != null) {
        String pluginSettingsEntityName = pluginSettings.getEntityType().getId();
        pluginSettingsCanWrite =
            permissionService.hasPermission(
                new EntityTypeIdentity(pluginSettingsEntityName), EntityTypePermission.UPDATE_DATA);
        modelAndView.addObject(PluginAttributes.KEY_PLUGIN_SETTINGS, pluginSettings);
      } else {
        pluginSettingsCanWrite = false;
      }

      modelAndView.addObject(PluginAttributes.KEY_PLUGIN_SHOW_SETTINGS_COG, pluginSettingsCanWrite);
      modelAndView.addObject(PluginAttributes.KEY_MENU, menuReaderService.getMenu().orElse(null));

      modelAndView.addObject(
          PluginAttributes.KEY_AUTHENTICATED, SecurityUtils.currentUserIsAuthenticated());
      modelAndView.addObject(
          PluginAttributes.KEY_PLUGIN_ID_WITH_QUERY_STRING,
          getPluginIdWithQueryString(request, pluginId));
    }
  }

  public PluginController validateHandler(Object handler) {
    if (!(handler instanceof HandlerMethod)) {
      throw new RuntimeException("handler is not of type " + HandlerMethod.class.getSimpleName());
    }
    Object bean = ((HandlerMethod) handler).getBean();
    if (!(bean instanceof PluginController)) {
      throw new RuntimeException(
          "controller does not implement " + PluginController.class.getSimpleName());
    }
    return (PluginController) bean;
  }

  private String getPluginIdWithQueryString(HttpServletRequest request, String pluginId) {
    if (null != request) {
      String queryString = request.getQueryString();
      StringBuilder pluginIdAndQueryStringUrlPart = new StringBuilder();
      pluginIdAndQueryStringUrlPart.append(pluginId);
      if (queryString != null && !queryString.isEmpty())
        pluginIdAndQueryStringUrlPart.append('?').append(queryString);
      return pluginIdAndQueryStringUrlPart.toString();
    } else {
      return "";
    }
  }
}
