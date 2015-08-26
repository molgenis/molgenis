package org.molgenis.ui.browserdetection;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.UserAgent;

/**
 * Servlet filter that forwards the request to an browser unsupported message page if the user uses an unsupported
 * browser (< IE9)
 * 
 * IE compatibility mode is not taken into account (see https://github.com/molgenis/molgenis/issues/3481)
 */
public class BrowserDetectionFilter implements Filter
{
	private static final String USER_AGENT_HEADER_NAME = "User-Agent";
	private static final String UNSUPPORTED_BROWSER_MESSAGE_PAGE = "/html/unsupported-browser-message.html";
	private static final List<Browser> UNSUPPORTED_BROWSERS = Arrays.asList(Browser.IE5, Browser.IE5_5, Browser.IE6,
			Browser.IE7, Browser.IE8);

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException
	{
		HttpServletRequest httpRequest = (HttpServletRequest) request;

		if (!httpRequest.getRequestURI().startsWith("/api/")
				&& !isSupported(httpRequest.getHeader(USER_AGENT_HEADER_NAME)))
		{
			httpRequest.getRequestDispatcher(UNSUPPORTED_BROWSER_MESSAGE_PAGE).forward(request, response);
		}
		else
		{
			chain.doFilter(request, response);
		}
	}

	protected boolean isSupported(String userAgentHeaderValue)
	{
		UserAgent userAgent = UserAgent.parseUserAgentString(userAgentHeaderValue);
		return !UNSUPPORTED_BROWSERS.contains(userAgent.getBrowser());
	}

	@Override
	public void destroy()
	{
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException
	{
	}

}
