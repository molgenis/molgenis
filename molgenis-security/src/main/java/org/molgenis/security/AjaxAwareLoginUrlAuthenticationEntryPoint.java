package org.molgenis.security;

import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.login.MolgenisLoginController;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.ELRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author bchild
 */
public class AjaxAwareLoginUrlAuthenticationEntryPoint extends LoginUrlAuthenticationEntryPoint
{

	private static final RequestMatcher requestMatcher = new ELRequestMatcher(
			"hasHeader('X-Requested-With','XMLHttpRequest')");

	public AjaxAwareLoginUrlAuthenticationEntryPoint(String loginFormUrl)
	{
		super(loginFormUrl);
	}

	@Override
	public void commence(final HttpServletRequest request, final HttpServletResponse response,
			final AuthenticationException authException) throws IOException, ServletException
	{
		if (isPreflight(request))
		{
			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
		}
		else if (isRestRequest(request))
		{
			if (SecurityUtils.isSessionExpired(request))
			{
				// Signal that 'session expired' message must be shown to the user
				request.getSession().setAttribute(MolgenisLoginController.SESSION_EXPIRED_SESSION_ATTRIBUTE, true);
			}

			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
		}
		else
		{
			if (SecurityUtils.isSessionExpired(request))
			{
				// Signal that 'session expired' message must be shown to the user
				request.getSession().setAttribute("sessionExpired", true);
			}

			super.commence(request, response, authException);
		}
	}

	/**
	 * Checks if this is a X-domain pre-flight request.
	 *
	 * @param request
	 * @return
	 */
	private boolean isPreflight(HttpServletRequest request)
	{
		return "OPTIONS".equals(request.getMethod());
	}

	/**
	 * Checks if it is a rest request
	 *
	 * @param request
	 * @return
	 */
	protected boolean isRestRequest(HttpServletRequest request)
	{
		return requestMatcher.matches(request);
	}
}