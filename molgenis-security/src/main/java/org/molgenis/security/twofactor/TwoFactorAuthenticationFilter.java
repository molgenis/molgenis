package org.molgenis.security.twofactor;

import org.molgenis.data.settings.AppSettings;
import org.molgenis.security.core.utils.SecurityUtils;
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
	private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

	private static String PATH = "/2fa";
	private AppSettings appSettings;

	public TwoFactorAuthenticationFilter(AppSettings appSettings)
	{
		this.appSettings = requireNonNull(appSettings);
	}

	@Override
	protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
			FilterChain filterChain) throws ServletException, IOException
	{
		if (isTwoFactorAuthenticationEnabled())
		{
			//FIXME remove path hack
			if (!httpServletRequest.getRequestURI().equals(PATH) && SecurityUtils.currentUserIsAuthenticated()
					&& !SecurityUtils.currentUserHasRole("ROLE_TWO_FACTOR_AUTHENTICATED"))
			{
				//TODO replace url with controller path
				redirectStrategy.sendRedirect(httpServletRequest, httpServletResponse, PATH);
				return;
			}
		}

		filterChain.doFilter(httpServletRequest, httpServletResponse);
	}

	private boolean isTwoFactorAuthenticationEnabled()
	{
		return !appSettings.getTwoFactorAuthentication().equals(DISABLED.toString());
	}

}
