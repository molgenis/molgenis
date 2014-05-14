package org.molgenis.security;

import static org.molgenis.security.account.AccountController.CHANGE_PASSWORD_URI;
import static org.molgenis.security.core.utils.SecurityUtils.ANONYMOUS_USERNAME;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.auth.MolgenisUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.web.filter.GenericFilterBean;

public class MolgenisChangePasswordFilter extends GenericFilterBean
{
	private final DataService dataService;
	private final RedirectStrategy redirectStrategy;

	public MolgenisChangePasswordFilter(DataService dataService, RedirectStrategy redirectStrategy)
	{
		this.dataService = dataService;
		this.redirectStrategy = redirectStrategy;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException
	{
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if ((authentication != null) && authentication.isAuthenticated()
				&& !authentication.getName().equals(ANONYMOUS_USERNAME)
				&& !httpRequest.getRequestURI().toLowerCase().endsWith(CHANGE_PASSWORD_URI.toLowerCase())
				&& !httpRequest.getRequestURI().toLowerCase().startsWith("/img/")
				&& !httpRequest.getRequestURI().toLowerCase().startsWith("/css/")
				&& !httpRequest.getRequestURI().toLowerCase().startsWith("/js/"))
		{
			MolgenisUser user = dataService.findOne(MolgenisUser.ENTITY_NAME,
					new QueryImpl().eq(MolgenisUser.USERNAME, authentication.getName()), MolgenisUser.class);
			if (user == null)
			{
				throw new RuntimeException("Unknown username [" + authentication.getName() + "]");
			}

			if (user.getChangePassword() != null && user.getChangePassword().booleanValue())
			{
				redirectStrategy.sendRedirect(httpRequest, httpResponse, CHANGE_PASSWORD_URI);
				return;
			}
		}

		chain.doFilter(request, response);
	}

}
