package org.molgenis.search;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.molgenis.framework.security.Login;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * HandlerInterceptor returns SC_UNAUTHORIZED if no Login object is present in
 * the session or the user is not authenticated and login is required.
 * 
 * Does not check entity level security or roles. Just checks if the user is
 * authenticated
 * 
 * @author erwin
 * 
 */
public class SearchSecurityHandlerInterceptor extends HandlerInterceptorAdapter
{
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception
	{
		Login login = (Login) request.getSession().getAttribute("login");
		if ((login != null) && (login.isAuthenticated() || !login.isLoginRequired()))
		{
			return true;
		}

		response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		return false;
	}
}
