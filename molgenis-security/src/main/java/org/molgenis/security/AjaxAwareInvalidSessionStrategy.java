package org.molgenis.security;

import org.springframework.security.web.session.InvalidSessionStrategy;
import org.springframework.security.web.session.SimpleRedirectInvalidSessionStrategy;
import org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static java.util.Objects.requireNonNull;

/**
 * Same as {@link SimpleRedirectInvalidSessionStrategy} except for XMLHttpRequests in which case a status code is
 * returned instead of redirection.
 */
public class AjaxAwareInvalidSessionStrategy implements InvalidSessionStrategy
{
	private static final RequestMatcher REQUEST_MATCHER_XML_HTTP_REQUEST = new RequestHeaderRequestMatcher(
			"X-Requested-With", "XMLHttpRequest");

	private final SimpleRedirectInvalidSessionStrategy simpleRedirectInvalidSessionStrategy;

	AjaxAwareInvalidSessionStrategy(String invalidSessionUrl)
	{
		requireNonNull(invalidSessionUrl);
		this.simpleRedirectInvalidSessionStrategy = new SimpleRedirectInvalidSessionStrategy(invalidSessionUrl);
	}

	@Override
	public void onInvalidSessionDetected(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException
	{
		if (REQUEST_MATCHER_XML_HTTP_REQUEST.matches(request))
		{
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		}
		else
		{
			simpleRedirectInvalidSessionStrategy.onInvalidSessionDetected(request, response);
		}
	}
}
