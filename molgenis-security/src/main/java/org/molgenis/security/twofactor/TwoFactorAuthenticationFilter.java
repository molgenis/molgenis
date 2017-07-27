package org.molgenis.security.twofactor;

import org.molgenis.data.settings.AppSettings;
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

import static java.util.Objects.requireNonNull;
import static org.molgenis.security.twofactor.TwoFactorAuthenticationSetting.DISABLED;

public class TwoFactorAuthenticationFilter extends OncePerRequestFilter
{

	private static final Logger LOG = LoggerFactory.getLogger(TwoFactorAuthenticationFilter.class.getName());

	private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

	private AppSettings appSettings;
	private TwoFactorAuthenticationService twoFactorAuthenticationService;

	public TwoFactorAuthenticationFilter(AppSettings appSettings,
			TwoFactorAuthenticationService twoFactorAuthenticationService)
	{
		this.appSettings = requireNonNull(appSettings);
		this.twoFactorAuthenticationService = requireNonNull(twoFactorAuthenticationService);
	}

	@Override
	protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
			FilterChain filterChain) throws ServletException, IOException
	{
		if (isTwoFactorAuthenticationEnabled())
		{
			//FIXME remove path hack
			if (!httpServletRequest.getRequestURI().contains(TwoFactorAuthenticationController.URI)
					&& SecurityUtils.currentUserIsAuthenticated() && !SecurityUtils.currentUserHasRole(
					SecurityUtils.AUTHORITY_TWO_FACTOR_AUTHENTICATION))
			{
				if (twoFactorAuthenticationService.isConfiguredForUser())
				{
					redirectStrategy.sendRedirect(httpServletRequest, httpServletResponse,
							TwoFactorAuthenticationController.URI
									+ TwoFactorAuthenticationController.TWO_FACTOR_ENABLED_URI);
					return;
				}
				else
				{
					redirectStrategy.sendRedirect(httpServletRequest, httpServletResponse,
							TwoFactorAuthenticationController.URI
									+ TwoFactorAuthenticationController.TWO_FACTOR_INITIAL_URI);
					return;
				}
			}
		}
		else
		{
			LOG.debug("No 2 factor authentication configured");
		}

		filterChain.doFilter(httpServletRequest, httpServletResponse);
	}

	private boolean isTwoFactorAuthenticationEnabled()
	{
		return !appSettings.getTwoFactorAuthentication().equals(DISABLED.toString());
	}

}
