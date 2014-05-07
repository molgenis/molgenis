package org.molgenis.security.token;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

/**
 * Servlet filter that checks the httprequest for a molgenis security token and if valid logs the user in, else returns
 * a 401
 */
public class TokenAuthenticationFilter extends GenericFilterBean
{
	private final AuthenticationProvider authenticationProvider;

	public TokenAuthenticationFilter(AuthenticationProvider authenticationProvider)
	{
		this.authenticationProvider = authenticationProvider;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException
	{
		HttpServletRequest httpRequest = (HttpServletRequest) request;

		// Do not check login and logout methods
		if (!httpRequest.getRequestURI().endsWith("login") && !httpRequest.getRequestURI().endsWith("logout"))
		{
			// Get the token from the request
			String token = TokenExtractor.getToken(httpRequest);
			if (StringUtils.isNotBlank(token))
			{
				try
				{
					// Authenticate the token
					RestAuthenticationToken authToken = new RestAuthenticationToken(token);
					Authentication authentication = authenticationProvider.authenticate(authToken);
					if (authentication.isAuthenticated())
					{
						// Log user in
						SecurityContextHolder.getContext().setAuthentication(authentication);
					}
				}
				catch (AuthenticationException e)
				{
					((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
					return;
				}
			}
		}

		chain.doFilter(request, response);
	}
}
