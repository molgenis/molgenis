package org.molgenis.security.session;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.login.MolgenisLoginController;
import org.springframework.web.filter.GenericFilterBean;

/**
 * Check if the requested sessionId is valid for an api call.
 * 
 * If No JSESSIONID is provided it session is valid.
 * 
 * If session is invalid a 401 is returned to the client.
 * 
 * Because the REST api doesn't need authentication this Filter must be used in addition to the
 * AjaxAwareLoginUrlAuthenticationEntryPoint that only works on protected urls.
 */
public class ApiSessionExpirationFilter extends GenericFilterBean
{
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException
	{
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		if (httpRequest.getRequestURI().startsWith("/api/") && SecurityUtils.isSessionExpired(httpRequest))
		{
			// Signal that the 'session expired' message must be shown in the login form
			httpRequest.getSession().setAttribute(MolgenisLoginController.SESSION_EXPIRED_SESSION_ATTR, true);
			httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		}
		else
		{
			chain.doFilter(request, response);
		}
	}

}
