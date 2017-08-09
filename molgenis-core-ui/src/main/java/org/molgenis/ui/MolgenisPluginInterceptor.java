package org.molgenis.ui;

import org.molgenis.data.Entity;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static java.util.Objects.requireNonNull;
import static org.molgenis.ui.MolgenisPluginAttributes.*;

/**
 * Interceptor that adds default model objects to all plugin requests that return a view.
 */
public class MolgenisPluginInterceptor extends HandlerInterceptorAdapter
{
	private final MolgenisUi molgenisUi;
	private final MolgenisPermissionService permissionService;

	@Autowired
	public MolgenisPluginInterceptor(MolgenisUi molgenisUi, MolgenisPermissionService permissionService)
	{
		this.molgenisUi = requireNonNull(molgenisUi);
		this.permissionService = requireNonNull(permissionService);
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception
	{
		MolgenisPluginController molgenisPlugin = validateHandler(handler);

		// determine context url for this plugin if no context exists
		String contextUrl = (String) request.getAttribute(MolgenisPluginAttributes.KEY_CONTEXT_URL);
		if (contextUrl == null)
		{
			request.setAttribute(MolgenisPluginAttributes.KEY_CONTEXT_URL, molgenisPlugin.getUri());
		}

		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception
	{
		if (modelAndView != null)
		{
			MolgenisPluginController molgenisPlugin = validateHandler(handler);
			String pluginId = molgenisPlugin.getId();

			// allow controllers that handle multiple plugins to set their plugin id
			if (!modelAndView.getModel().containsKey(KEY_PLUGIN_ID))
			{
				modelAndView.addObject(KEY_PLUGIN_ID, pluginId);
			}

			Entity pluginSettings = molgenisPlugin.getPluginSettings();
			Boolean pluginSettingsCanWrite;
			if (pluginSettings != null)
			{
				String pluginSettingsEntityName = pluginSettings.getEntityType().getId();
				pluginSettingsCanWrite = permissionService.hasPermissionOnEntity(pluginSettingsEntityName,
						Permission.WRITE);
			}
			else
			{
				pluginSettingsCanWrite = null;
			}

			modelAndView.addObject(KEY_PLUGIN_SETTINGS, pluginSettings);
			modelAndView.addObject(KEY_PLUGIN_SETTINGS_CAN_WRITE, pluginSettingsCanWrite);
			modelAndView.addObject(KEY_MOLGENIS_UI, molgenisUi);
			modelAndView.addObject(KEY_AUTHENTICATED, SecurityUtils.currentUserIsAuthenticated());
			modelAndView.addObject(KEY_PLUGIN_ID_WITH_QUERY_STRING, getPluginIdWithQueryString(request, pluginId));
		}
	}

	public MolgenisPluginController validateHandler(Object handler)
	{
		if (!(handler instanceof HandlerMethod))
		{
			throw new RuntimeException("handler is not of type " + HandlerMethod.class.getSimpleName());
		}
		Object bean = ((HandlerMethod) handler).getBean();
		if (!(bean instanceof MolgenisPluginController))
		{
			throw new RuntimeException(
					"controller does not implement " + MolgenisPluginController.class.getSimpleName());
		}
		return (MolgenisPluginController) bean;
	}

	private String getPluginIdWithQueryString(HttpServletRequest request, String pluginId)
	{
		if (null != request)
		{
			String queryString = request.getQueryString();
			StringBuilder pluginIdAndQueryStringUrlPart = new StringBuilder();
			pluginIdAndQueryStringUrlPart.append(pluginId);
			if (queryString != null && !queryString.isEmpty())
				pluginIdAndQueryStringUrlPart.append('?').append(queryString);
			return pluginIdAndQueryStringUrlPart.toString();
		}
		else
		{
			return "";
		}
	}
}
