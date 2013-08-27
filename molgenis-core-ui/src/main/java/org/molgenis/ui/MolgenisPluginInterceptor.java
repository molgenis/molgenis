package org.molgenis.ui;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.molgenis.framework.security.Login;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

public class MolgenisPluginInterceptor extends HandlerInterceptorAdapter
{
	static final String KEY_PLUGIN_ID = "plugin_id";
	static final String KEY_MOLGENIS_UI = "molgenis_ui";
	static final String KEY_AUTHENTICATED = "authenticated";

	private final Login login;
	private final MolgenisUi molgenisUi;

	@Autowired
	public MolgenisPluginInterceptor(Login login, MolgenisUi molgenisUi)
	{
		if (login == null) throw new IllegalArgumentException("login is null");
		if (molgenisUi == null) throw new IllegalArgumentException("molgenis ui is null");
		this.login = login;
		this.molgenisUi = molgenisUi;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception
	{
		if (modelAndView != null)
		{
			if (!(handler instanceof HandlerMethod))
			{
				throw new RuntimeException("handler is not of type " + HandlerMethod.class.getSimpleName());
			}
			Object bean = ((HandlerMethod) handler).getBean();
			if (!(bean instanceof MolgenisPluginController))
			{
				throw new RuntimeException("controller does not implement "
						+ MolgenisPluginController.class.getSimpleName());
			}
			modelAndView.addObject(KEY_PLUGIN_ID, ((MolgenisPluginController) bean).getId());
			modelAndView.addObject(KEY_MOLGENIS_UI, molgenisUi);
			modelAndView.addObject(KEY_AUTHENTICATED, login.isAuthenticated());
			// TODO remove flag after removing molgenis UI framework
			modelAndView.addObject("enable_spring_ui", MolgenisRootController.USE_SPRING_UI);
		}
	}
}
