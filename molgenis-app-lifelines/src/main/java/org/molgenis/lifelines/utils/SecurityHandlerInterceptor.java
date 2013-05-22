package org.molgenis.lifelines.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.molgenis.framework.security.Login;
import org.molgenis.framework.ui.ScreenController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * HandlerInterceptor that adds authorization to spring controllers.
 * 
 * For use with the IFramePlugin. Checks if there is a user logged in and has
 * the right to read a ScreenController
 * 
 * @author erwin
 * 
 */
public class SecurityHandlerInterceptor extends HandlerInterceptorAdapter
{
	private Login login;
	private final Class<? extends ScreenController<?>> screenControllerClass;

	@Autowired
	public void setLogin(Login login)
	{
		this.login = login;
	}

	public SecurityHandlerInterceptor(Class<? extends ScreenController<?>> screenControllerClass)
	{
		this.screenControllerClass = screenControllerClass;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception
	{
		if ((login != null) && login.canReadScreenController(screenControllerClass))
		{
			return true;
		}

		response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		return false;
	}
}
