package org.molgenis.security.twofactor.auth;

import org.molgenis.data.settings.AppSettings;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.token.RestAuthenticationToken;
import org.molgenis.security.twofactor.TwoFactorAuthenticationController;
import org.molgenis.security.twofactor.service.TwoFactorAuthenticationService;
import org.molgenis.security.user.UserAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static java.util.Objects.requireNonNull;
import static org.molgenis.security.twofactor.auth.TwoFactorAuthenticationSetting.DISABLED;
import static org.molgenis.security.twofactor.auth.TwoFactorAuthenticationSetting.ENFORCED;

public class TwoFactorAuthenticationFilter extends OncePerRequestFilter
{
	private static final Logger LOG = LoggerFactory.getLogger(TwoFactorAuthenticationFilter.class.getName());

	private final RedirectStrategy redirectStrategy;
	private final UserAccountService userAccountService;

	private final AppSettings appSettings;
	private final TwoFactorAuthenticationService twoFactorAuthenticationService;

	public TwoFactorAuthenticationFilter(AppSettings appSettings,
			TwoFactorAuthenticationService twoFactorAuthenticationService, RedirectStrategy redirectStrategy,
			UserAccountService userAccountService)
	{
		this.appSettings = requireNonNull(appSettings);
		this.twoFactorAuthenticationService = requireNonNull(twoFactorAuthenticationService);
		this.redirectStrategy = requireNonNull(redirectStrategy);
		this.userAccountService = requireNonNull(userAccountService);
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
				if (!isUserTwoFactorAuthenticated() && !hasAuthenticatedMolgenisToken()
						&& !isUserRecoveryAuthenticated())
				{
					if (isTwoFactorAuthenticationEnforced() || userUsesTwoFactorAuthentication())
					{
						if (twoFactorAuthenticationService.isConfiguredForUser())
						{
							redirectStrategy.sendRedirect(httpServletRequest, httpServletResponse,
									TwoFactorAuthenticationController.URI
											+ TwoFactorAuthenticationController.TWO_FACTOR_CONFIGURED_URI);
							return;
						}
						else
						{
							redirectStrategy.sendRedirect(httpServletRequest, httpServletResponse,
									TwoFactorAuthenticationController.URI
											+ TwoFactorAuthenticationController.TWO_FACTOR_ACTIVATION_URI);
							return;
						}
					}
				}
			}
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

	private boolean userUsesTwoFactorAuthentication()
	{
		return userAccountService.getCurrentUser().isTwoFactorAuthentication();
	}

	private boolean isUserTwoFactorAuthenticated()
	{
		boolean isTwoFactorAuthenticated = false;
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication instanceof TwoFactorAuthenticationToken)
		{
			isTwoFactorAuthenticated = authentication.isAuthenticated();
		}

		return isTwoFactorAuthenticated;
	}

	private boolean isUserRecoveryAuthenticated()
	{
		boolean isRecoveryAuthenticated = false;
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication instanceof RecoveryAuthenticationToken)
		{
			isRecoveryAuthenticated = authentication.isAuthenticated();
		}

		return isRecoveryAuthenticated;
	}

	/**
	 * Check on authenticated RestAuthenticationToken
	 *
	 * @return authenticated {@link RestAuthenticationToken}
	 */
	private boolean hasAuthenticatedMolgenisToken()
	{
		boolean hasAuthenticatedMolgenisToken = false;
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication instanceof RestAuthenticationToken)
		{
			hasAuthenticatedMolgenisToken = authentication.isAuthenticated();
		}
		return hasAuthenticatedMolgenisToken;
	}
}
