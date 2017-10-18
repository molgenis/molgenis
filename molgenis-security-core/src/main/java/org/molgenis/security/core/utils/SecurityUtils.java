package org.molgenis.security.core.utils;

import com.google.common.collect.Sets;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.molgenis.security.core.runas.SystemSecurityToken.ROLE_SYSTEM;

public class SecurityUtils
{
	public static final String ANONYMOUS_USERNAME = "anonymous";

	public static final String AUTHORITY_SU = "ROLE_SU";
	public static final String AUTHORITY_ANONYMOUS = "ROLE_ANONYMOUS";

	private SecurityUtils()
	{
	}

	/**
	 * Returns the username of the user present in the Authentication.
	 * N.B. This does *not* mean that the user is authenticated!
	 *
	 * @return String with the username, or empty if not present
	 */
	public static Optional<String> getCurrentUsername()
	{
		return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication()).map(SecurityUtils::getUsername);
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
		Set<String> rolesSet = Sets.newHashSet(roles);
		return getAuthenticationFromContext().map(Stream::of)
											 .orElse(Stream.empty())
											 .map(Authentication::getAuthorities)
											 .flatMap(Collection::stream)
											 .map(GrantedAuthority::getAuthority)
											 .anyMatch(rolesSet::contains);
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
		return authentication != null && authentication.isAuthenticated() && !currentUserHasRole(AUTHORITY_ANONYMOUS);
	}

	public static Optional<Authentication> getAuthenticationFromContext()
	{
		return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication());
	}
}
