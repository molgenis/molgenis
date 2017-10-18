package org.molgenis.security;

import org.molgenis.security.core.model.User;
import org.molgenis.security.core.service.UserService;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.molgenis.security.account.AccountController.CHANGE_PASSWORD_URI;
import static org.molgenis.security.core.utils.SecurityUtils.ANONYMOUS_USERNAME;

public class MolgenisChangePasswordFilter extends GenericFilterBean
{
	private final UserService userService;
	private final RedirectStrategy redirectStrategy;

	public MolgenisChangePasswordFilter(UserService userService, RedirectStrategy redirectStrategy)
	{
		this.userService = userService;
		this.redirectStrategy = redirectStrategy;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException
	{
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		if (!isPasswordChangeRequest(httpRequest) && currentUserNeedsPasswordChange())
		{
			redirectStrategy.sendRedirect(httpRequest, httpResponse, CHANGE_PASSWORD_URI);
		}
		chain.doFilter(request, response);
	}

	private boolean isPasswordChangeRequest(HttpServletRequest httpRequest)
	{
		return httpRequest.getRequestURI().equalsIgnoreCase(CHANGE_PASSWORD_URI);
	}

	private boolean currentUserNeedsPasswordChange()
	{
		return SecurityUtils.getAuthenticationFromContext()
							.filter(Authentication::isAuthenticated)
							.map(Authentication::getName)
							.filter(name -> !name.equals(ANONYMOUS_USERNAME))
							.map(userService::findByUsername)
							.filter(User::isChangePassword)
							.isPresent();
	}

}
