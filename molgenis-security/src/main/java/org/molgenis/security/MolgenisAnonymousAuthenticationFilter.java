package org.molgenis.security;

import org.molgenis.security.core.runas.SystemSecurityToken;
import org.molgenis.security.core.utils.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.Assert;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collection;

/**
 * Based on org.springframework.security.web.authentication.AnonymousAuthenticationFilter:
 * <p>
 * Detects if there is no {@code Authentication} object in the {@code SecurityContextHolder}, and populates it with one
 * if needed.
 */
public class MolgenisAnonymousAuthenticationFilter extends GenericFilterBean implements InitializingBean
{
	private static final Logger LOG = LoggerFactory.getLogger(MolgenisAnonymousAuthenticationFilter.class);

	// ~ Instance fields
	// ================================================================================================

	private AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource = new WebAuthenticationDetailsSource();
	private final String key;
	private final Object principal;
	private final UserDetailsService userDetailsService;

	/**
	 * Creates a filter with a principal named "anonymousUser" and the single authority "ROLE_ANONYMOUS".
	 *
	 * @param key the key to identify tokens created by this filter
	 */
	public MolgenisAnonymousAuthenticationFilter(String key, Object principal, UserDetailsService userDetailsService)
	{
		this.key = key;
		this.principal = principal;
		this.userDetailsService = userDetailsService;
	}

	// ~ Methods
	// ========================================================================================================

	@Override
	public void afterPropertiesSet()
	{
		Assert.hasLength(key,
				"[Assertion failed] - this String argument must have length; it must not be null or empty");
		Assert.notNull(principal, "Anonymous authentication principal must be set");
		Assert.notNull(userDetailsService, "User details service must be set");
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException
	{
		if (SecurityContextHolder.getContext().getAuthentication() == null)
		{
			SecurityContextHolder.getContext().setAuthentication(createAuthentication((HttpServletRequest) req));

			if (LOG.isDebugEnabled())
			{
				LOG.debug("Populated SecurityContextHolder with anonymous token: '" + SecurityContextHolder.getContext()
																										   .getAuthentication()
						+ "'");
			}
		}
		else
		{
			if (LOG.isTraceEnabled())
			{
				LOG.trace("SecurityContextHolder not populated with anonymous token, as it already contained: '{}'",
						SecurityContextHolder.getContext().getAuthentication());
			}
		}

		chain.doFilter(req, res);
	}

	protected Authentication createAuthentication(HttpServletRequest request)
	{
		AnonymousAuthenticationToken auth = new AnonymousAuthenticationToken(key, principal, getAuthorities());
		auth.setDetails(authenticationDetailsSource.buildDetails(request));
		return auth;
	}

	public void setAuthenticationDetailsSource(
			AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource)
	{
		Assert.notNull(authenticationDetailsSource, "AuthenticationDetailsSource required");
		this.authenticationDetailsSource = authenticationDetailsSource;
	}

	public Object getPrincipal()
	{
		return principal;
	}

	public Collection<? extends GrantedAuthority> getAuthorities()
	{
		// Remember the original context
		SecurityContext origCtx = SecurityContextHolder.getContext();
		try
		{
			// Set a SystemSecurityToken
			SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext());
			SecurityContextHolder.getContext().setAuthentication(new SystemSecurityToken());

			UserDetails user = userDetailsService.loadUserByUsername(SecurityUtils.ANONYMOUS_USERNAME);
			if (user == null)
			{
				throw new RuntimeException("user with name '" + SecurityUtils.ANONYMOUS_USERNAME + "' does not exist");
			}
			return user.getAuthorities();
		}
		finally
		{
			// Set the original context back when method is finished
			SecurityContextHolder.setContext(origCtx);
		}
	}
}
