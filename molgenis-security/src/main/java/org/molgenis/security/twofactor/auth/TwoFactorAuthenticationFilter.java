package org.molgenis.security.twofactor.auth;

import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.token.RestAuthenticationToken;
import org.molgenis.security.twofactor.TwoFactorAuthenticationController;
import org.molgenis.security.twofactor.service.TwoFactorAuthenticationService;
import org.molgenis.security.settings.AuthenticationSettings;
import org.molgenis.security.user.UserAccountService;
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
	private final RedirectStrategy redirectStrategy;
	private final UserAccountService userAccountService;

	private final TwoFactorAuthenticationService twoFactorAuthenticationService;
	private final AuthenticationSettings authenticationSettings;

	public TwoFactorAuthenticationFilter(AuthenticationSettings authenticationSettings,
			TwoFactorAuthenticationService twoFactorAuthenticationService, RedirectStrategy redirectStrategy,
			UserAccountService userAccountService)
	{
		this.authenticationSettings = requireNonNull(authenticationSettings);
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
		return !(authenticationSettings.getTwoFactorAuthentication() == DISABLED);
	}

	private boolean isTwoFactorAuthenticationEnforced()
	{
		return authenticationSettings.getTwoFactorAuthentication() == ENFORCED;
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
