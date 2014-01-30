package org.molgenis.data.rest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.molgenis.security.SecurityUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * HandlerInterceptor that returns UNAUTHENTICATED when the current user is not authenticated or is the anonymous user
 * 
 * @author erwin
 * 
 */
public class RestApiSecurityHandlerInterceptor extends HandlerInterceptorAdapter
{
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception
	{
		// Check if user is authenticated
		if (SecurityUtils.currentUserIsAuthenticated())
		{
			return true;
		}

		response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		return false;
	}
}
