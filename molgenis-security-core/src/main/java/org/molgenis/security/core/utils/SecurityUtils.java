package org.molgenis.security.core.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

import static org.molgenis.security.core.runas.SystemSecurityToken.ROLE_SYSTEM;

public class SecurityUtils
{
	public static final String ANONYMOUS_USERNAME = "anonymous";

	public static final String AUTHORITY_SU = "ROLE_SU";
	public static final String AUTHORITY_ANONYMOUS = "ROLE_ANONYMOUS";

	public static final String ROLE_ACL_TAKE_OWNERSHIP = "ROLE_ACL_TAKE_OWNERSHIP";
	public static final String ROLE_ACL_MODIFY_AUDITING = "ROLE_ACL_MODIFY_AUDITING";
	public static final String ROLE_ACL_GENERAL_CHANGES = "ROLE_ACL_GENERAL_CHANGES";

	public static String getCurrentUsername()
	{
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null)
		{
			return null;
		}
		return getUsername(authentication);
	}

	public static String getUsername(Authentication authentication)
	{
		Object principal = authentication.getPrincipal();
		if (principal instanceof UserDetails)
		{
			return ((UserDetails) principal).getUsername();
		}

		return principal.toString();
	}

	/**
	 * Returns whether the current user has at least one of the given roles
	 */
	public static boolean currentUserHasRole(String... roles)
	{
		if (roles == null || roles.length == 0) return false;

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null)
		{
			Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
			if (authorities == null) throw new IllegalStateException("No user currently logged in");

			for (String role : roles)
			{
				for (GrantedAuthority grantedAuthority : authorities)
				{
					if (role.equals(grantedAuthority.getAuthority())) return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns whether the current user is a superuser or the system user.
	 */
	public static boolean currentUserIsSuOrSystem()
	{
		return currentUserIsSu() || currentUserIsSystem();
	}

	/**
	 * Returns whether the current user is a super user
	 */
	public static boolean currentUserIsSu()
	{
		return currentUserHasRole(AUTHORITY_SU);
	}

	/**
	 * Returns whether the current user is the system user.
	 */
	public static boolean currentUserIsSystem()
	{
		return currentUserHasRole(ROLE_SYSTEM);
	}

	/**
	 * Returns whether the current user is authenticated and not the anonymous user
	 */
	public static boolean currentUserIsAuthenticated()
	{
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication != null && authentication.isAuthenticated() && !currentUserIsAnonymous();
	}

	private static boolean currentUserIsAnonymous()
	{
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication == null || currentUserHasRole(AUTHORITY_ANONYMOUS);
	}
}
