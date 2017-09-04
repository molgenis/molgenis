package org.molgenis.security.session;

import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.login.MolgenisLoginController;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Check if the requested sessionId is valid for an api call.
 * <p>
 * If No JSESSIONID is provided it session is valid.
 * <p>
 * If session is invalid a 401 is returned to the client.
 * <p>
 * Because the REST api doesn't need authentication this Filter must be used in addition to the
 * AjaxAwareLoginUrlAuthenticationEntryPoint that only works on protected urls.
 */
public class ApiSessionExpirationFilter extends GenericFilterBean
{
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException
	{
		HttpServletRequest httpRequest = (HttpServletRequest) request;

		if (!SecurityUtils.currentUserIsAuthenticated() && httpRequest.getRequestURI().startsWith("/api/")
				&& SecurityUtils.isSessionExpired(httpRequest) && !httpRequest.getRequestURI()
																			  .startsWith("/api/v1/login")
				&& !httpRequest.getRequestURI().startsWith("/api/v1/logout") && !httpRequest.getRequestURI()
																							.startsWith(
																									"/api/v2/version"))
		{
			// Signal that the 'session expired' message must be shown in the login form
			httpRequest.getSession().setAttribute(MolgenisLoginController.SESSION_EXPIRED_SESSION_ATTRIBUTE, true);
		}
		chain.doFilter(request, response);
	}

}
