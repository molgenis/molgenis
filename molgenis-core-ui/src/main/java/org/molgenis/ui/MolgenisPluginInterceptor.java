package org.molgenis.ui;

import static org.molgenis.ui.MolgenisPluginAttributes.KEY_AUTHENTICATED;
import static org.molgenis.ui.MolgenisPluginAttributes.KEY_MOLGENIS_UI;
import static org.molgenis.ui.MolgenisPluginAttributes.KEY_PLUGIN_ID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.molgenis.framework.db.DatabaseAccessException;
import org.molgenis.framework.security.Login;
import org.molgenis.framework.server.MolgenisPermissionService;
import org.molgenis.framework.server.MolgenisPermissionService.Permission;
import org.molgenis.framework.ui.MolgenisPlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

public class MolgenisPluginInterceptor extends HandlerInterceptorAdapter
{
	private final Login login;
	private final MolgenisPermissionService permissionService;
	private final MolgenisUi molgenisUi;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception
	{
		MolgenisPlugin molgenisPlugin = validateHandler(handler);

		// determine is access to plugin is allowed
		boolean permission = permissionService.hasPermissionOnPlugin(molgenisPlugin.getClass(), Permission.READ);
		if (!permission) throw new DatabaseAccessException("access denied to " + request.getRequestURI());

		// determine context url for this plugin if no context exists
		String contextUrl = (String) request.getAttribute(MolgenisPluginAttributes.KEY_CONTEXT_URL);
		if (contextUrl == null)
		{
			request.setAttribute(MolgenisPluginAttributes.KEY_CONTEXT_URL, molgenisPlugin.getUri());
		}

		return true;
	}

	@Autowired
	public MolgenisPluginInterceptor(Login login, MolgenisPermissionService permissionService, MolgenisUi molgenisUi)
	{
		if (login == null) throw new IllegalArgumentException("login is null");
		if (permissionService == null) throw new IllegalArgumentException("permission service is null");
		if (molgenisUi == null) throw new IllegalArgumentException("molgenis ui is null");
		this.login = login;
		this.permissionService = permissionService;
		this.molgenisUi = molgenisUi;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception
	{
		if (modelAndView != null)
		{
			MolgenisPlugin molgenisPlugin = validateHandler(handler);

			// allow controllers that handle multiple plugins to set their plugin id
			if (!modelAndView.getModel().containsKey(KEY_PLUGIN_ID))
			{
				modelAndView.addObject(KEY_PLUGIN_ID, molgenisPlugin.getId());
			}
			modelAndView.addObject(KEY_MOLGENIS_UI, molgenisUi);
			modelAndView.addObject(KEY_AUTHENTICATED, login.isAuthenticated());
		}
	}

	public MolgenisPlugin validateHandler(Object handler)
	{
		if (!(handler instanceof HandlerMethod))
		{
			throw new RuntimeException("handler is not of type " + HandlerMethod.class.getSimpleName());
		}
		Object bean = ((HandlerMethod) handler).getBean();
		if (!(bean instanceof MolgenisPlugin))
		{
			throw new RuntimeException("controller does not implement " + MolgenisPlugin.class.getSimpleName());
		}
		return (MolgenisPlugin) bean;
	}
}
