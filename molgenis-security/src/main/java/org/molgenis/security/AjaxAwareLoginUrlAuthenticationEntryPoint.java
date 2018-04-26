package org.molgenis.security;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher;
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
	private static final RequestMatcher REQUEST_MATCHER_XML_HTTP_REQUEST = new RequestHeaderRequestMatcher(
			"X-Requested-With", "XMLHttpRequest");

	AjaxAwareLoginUrlAuthenticationEntryPoint(String loginFormUrl)
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
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		}
		else
		{
			super.commence(request, response, authException);
		}
	}

	/**
	 * Checks if this is a X-domain pre-flight request.
	 */
	private boolean isPreflight(HttpServletRequest request)
	{
		return "OPTIONS".equals(request.getMethod());
	}

	/**
	 * Checks if it is a rest request
	 */
	private boolean isRestRequest(HttpServletRequest request)
	{
		return REQUEST_MATCHER_XML_HTTP_REQUEST.matches(request);
	}
}