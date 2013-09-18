package org.molgenis.search;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.security.SecurityUtils;
import org.molgenis.util.ApplicationContextProvider;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * HandlerInterceptor returns SC_UNAUTHORIZED if the user is not authenticated and login is required.
 * 
 * Does not check entity level security or roles. Just checks if the user is authenticated
 * 
 * @author erwin
 * 
 */
public class SearchSecurityHandlerInterceptor extends HandlerInterceptorAdapter
{
	private static final Logger logger = Logger.getLogger(SearchSecurityHandlerInterceptor.class);

	public static final String KEY_ACTION_ALLOW_ANONYMOUS_SEARCH = "api.search.allow.anonymous";
	private static final boolean DEFAULT_ACTION_ALLOW_ANONYMOUS_SEARCH = false;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception
	{
		if (isAllowAnonymousSearch() || SecurityUtils.currentUserIsAuthenticated())
		{
			return true;
		}

		response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		return false;
	}

	private boolean isAllowAnonymousSearch()
	{
		try
		{
			MolgenisSettings molgenisSettings = ApplicationContextProvider.getApplicationContext().getBean(
					MolgenisSettings.class);
			String property = molgenisSettings.getProperty(KEY_ACTION_ALLOW_ANONYMOUS_SEARCH,
					Boolean.toString(DEFAULT_ACTION_ALLOW_ANONYMOUS_SEARCH));
			return Boolean.valueOf(property);
		}
		catch (NoSuchBeanDefinitionException e)
		{
			logger.warn(e);
			return DEFAULT_ACTION_ALLOW_ANONYMOUS_SEARCH;
		}
	}
}
