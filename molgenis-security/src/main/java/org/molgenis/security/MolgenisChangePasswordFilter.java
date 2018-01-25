package org.molgenis.security;

import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.user.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if ((authentication != null) && authentication.isAuthenticated() && !authentication.getName()
																						   .equals(ANONYMOUS_USERNAME)
				&& !httpRequest.getRequestURI().equalsIgnoreCase(CHANGE_PASSWORD_URI))
		{
			User user = userService.getUser(authentication.getName());
			if (user == null)
			{
				throw new RuntimeException("Unknown username [" + authentication.getName() + "]");
			}

			if (user.isChangePassword() != null && user.isChangePassword())
			{
				redirectStrategy.sendRedirect(httpRequest, httpResponse, CHANGE_PASSWORD_URI);
				return;
			}
		}

		chain.doFilter(request, response);
	}

}
