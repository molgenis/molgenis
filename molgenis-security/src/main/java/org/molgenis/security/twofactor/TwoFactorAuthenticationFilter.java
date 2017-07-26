package org.molgenis.security.twofactor;

import org.molgenis.data.settings.AppSettings;
import org.molgenis.security.MolgenisAnonymousAuthenticationFilter;
import org.molgenis.security.core.utils.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class TwoFactorAuthenticationFilter extends OncePerRequestFilter
{
	private static final Logger LOG = LoggerFactory.getLogger(TwoFactorAuthenticationFilter.class);

	private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

	private static String PATH = "/2fa";
	private AppSettings appSettings;

	public TwoFactorAuthenticationFilter(AppSettings appSettings) {
		this.appSettings = appSettings;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
			FilterChain filterChain) throws ServletException, IOException
	{

		if(appSettings.is2FAEnabled()) {
			//FIXME remove path hack
			if (!httpServletRequest.getRequestURI().equals(PATH) && SecurityUtils.currentUserIsAuthenticated()
					&& !SecurityUtils.currentUserHasRole("ROLE_TWO_FACTOR_AUTHENTICATED"))
			{
				//TODO replace url with controller path
				redirectStrategy.sendRedirect(httpServletRequest, httpServletResponse, PATH);
				return;
			}
		} else {
			LOG.debug("2 factor authentication is not enabled");
		}
		filterChain.doFilter(httpServletRequest, httpServletResponse);
}
