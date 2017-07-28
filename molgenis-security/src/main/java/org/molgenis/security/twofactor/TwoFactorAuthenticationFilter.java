package org.molgenis.security.twofactor;

import org.molgenis.data.settings.AppSettings;
import org.molgenis.security.core.utils.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static java.util.Objects.requireNonNull;
import static org.molgenis.security.twofactor.TwoFactorAuthenticationSetting.DISABLED;
import static org.molgenis.security.twofactor.TwoFactorAuthenticationSetting.ENFORCED;

public class TwoFactorAuthenticationFilter extends OncePerRequestFilter
{

	private static final Logger LOG = LoggerFactory.getLogger(TwoFactorAuthenticationFilter.class.getName());

	private final RedirectStrategy redirectStrategy;

	private AppSettings appSettings;
	private TwoFactorAuthenticationService twoFactorAuthenticationService;

	public TwoFactorAuthenticationFilter(AppSettings appSettings,
			TwoFactorAuthenticationService twoFactorAuthenticationService, RedirectStrategy redirectStrategy)
	{
		this.appSettings = requireNonNull(appSettings);
		this.twoFactorAuthenticationService = requireNonNull(twoFactorAuthenticationService);
		this.redirectStrategy = requireNonNull(redirectStrategy);
	}

	@Override
	protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
			FilterChain filterChain) throws ServletException, IOException
	{
		if (isTwoFactorAuthenticationEnabled())
		{
			if (!httpServletRequest.getRequestURI().contains(TwoFactorAuthenticationController.URI)
					&& SecurityUtils.currentUserIsAuthenticated())
			{
				if (!isUserTwoFactorAuthenticated())
				{
					if (isTwoFactorAuthenticationEnforced() || userUsesTwoFactorAuthentication())
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

	private boolean isTwoFactorAuthenticationEnforced()
	{
		return appSettings.getTwoFactorAuthentication().equals(ENFORCED.toString());
	}

	//TODO add 2fa option for users
	private boolean userUsesTwoFactorAuthentication()
	{
		return true;
	}

	private boolean isUserTwoFactorAuthenticated()
	{
		return SecurityUtils.currentUserHasRole(SecurityUtils.AUTHORITY_TWO_FACTOR_AUTHENTICATION);
	}
}
