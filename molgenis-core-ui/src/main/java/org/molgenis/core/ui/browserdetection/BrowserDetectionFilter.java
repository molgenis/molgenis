package org.molgenis.core.ui.browserdetection;

import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.UserAgent;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Servlet filter that forwards the request to an browser unsupported message page if the user uses an unsupported
 * browser (< IE9)
 * <p>
 * IE compatibility mode is not taken into account (see https://github.com/molgenis/molgenis/issues/3481)
 */
public class BrowserDetectionFilter implements Filter
{
	private static final String USER_AGENT_HEADER_NAME = "User-Agent";
	private static final String UNSUPPORTED_BROWSER_MESSAGE_PAGE = "/html/unsupported-browser-message.html";
	private static final List<Browser> UNSUPPORTED_BROWSERS = Arrays.asList(Browser.IE5, Browser.IE5_5, Browser.IE6,
			Browser.IE7, Browser.IE8);
	private static final String CONTINUE_WITH_UNSUPPORTED_BROWSER_TOKEN = "continueWithUnsupportedBrowser";

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException
	{
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		if (!httpRequest.getRequestURI().startsWith("/api/") && !isSupported(
				httpRequest.getHeader(USER_AGENT_HEADER_NAME)))
		{
			HttpSession session = httpRequest.getSession();
			if (session.getAttribute(CONTINUE_WITH_UNSUPPORTED_BROWSER_TOKEN) == null)
			{
				if (request.getParameter(CONTINUE_WITH_UNSUPPORTED_BROWSER_TOKEN) != null)
				{
					session.setAttribute(CONTINUE_WITH_UNSUPPORTED_BROWSER_TOKEN, true);
				}
				else
				{
					httpRequest.getRequestDispatcher(UNSUPPORTED_BROWSER_MESSAGE_PAGE).forward(request, response);
					return;
				}
			}
		}

		chain.doFilter(request, response);
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
