package org.molgenis.security.core.utils;

import org.molgenis.security.core.Permission;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.molgenis.security.core.runas.SystemSecurityToken.ROLE_SYSTEM;

public class SecurityUtils
{
	public static final String ANONYMOUS_USERNAME = "anonymous";

	public static final String AUTHORITY_SU = "ROLE_SU";
	public static final String AUTHORITY_ANONYMOUS = "ROLE_ANONYMOUS";

	public static final String AUTHORITY_PLUGIN_PREFIX = "ROLE_PLUGIN_";
	public static final String AUTHORITY_PLUGIN_READ_PREFIX = AUTHORITY_PLUGIN_PREFIX + Permission.READ + "_";
	public static final String AUTHORITY_PLUGIN_WRITE_PREFIX = AUTHORITY_PLUGIN_PREFIX + Permission.WRITE + "_";
	public static final String AUTHORITY_PLUGIN_COUNT_PREFIX = AUTHORITY_PLUGIN_PREFIX + Permission.COUNT + "_";
	public static final String AUTHORITY_PLUGIN_WRITEMETA_PREFIX = AUTHORITY_PLUGIN_PREFIX + Permission.WRITEMETA + "_";

	public static final String AUTHORITY_ENTITY_PREFIX = "ROLE_ENTITY_";
	public static final String AUTHORITY_ENTITY_READ_PREFIX = AUTHORITY_ENTITY_PREFIX + Permission.READ + "_";
	public static final String AUTHORITY_ENTITY_WRITE_PREFIX = AUTHORITY_ENTITY_PREFIX + Permission.WRITE + "_";
	public static final String AUTHORITY_ENTITY_COUNT_PREFIX = AUTHORITY_ENTITY_PREFIX + Permission.COUNT + "_";
	public static final String AUTHORITY_ENTITY_WRITEMETA_PREFIX = AUTHORITY_ENTITY_PREFIX + Permission.WRITEMETA + "_";

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
	 *
	 * @param roles
	 * @return
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
	 *
	 * @return
	 */
	public static boolean currentUserIsSu()
	{
		return currentUserHasRole(AUTHORITY_SU);
	}

	/**
	 * Returns whether the current user is the system user.
	 *
	 * @return
	 */
	public static boolean currentUserIsSystem()
	{
		return currentUserHasRole(ROLE_SYSTEM);
	}

	/**
	 * Returns whether the current user is authenticated and not the anonymous user
	 *
	 * @return
	 */
	public static boolean currentUserIsAuthenticated()
	{
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication != null && authentication.isAuthenticated() && !currentUserHasRole(AUTHORITY_ANONYMOUS);
	}

	/**
	 * Returns the default (su, read, write) roles related to a plugin
	 *
	 * @param pluginIds
	 * @return
	 */
	public static String[] defaultPluginAuthorities(String... pluginIds)
	{
		List<String> pluginAuthorities = new ArrayList<>();
		pluginAuthorities.add(AUTHORITY_SU);
		if (pluginIds != null)
		{
			for (String pluginId : pluginIds)
			{
				pluginAuthorities.add(getPluginReadAuthority(pluginId));
				pluginAuthorities.add(getPluginWriteAuthority(pluginId));
			}
		}
		return pluginAuthorities.toArray(new String[] {});
	}

	public static String getPluginReadAuthority(String pluginId)
	{
		return AUTHORITY_PLUGIN_READ_PREFIX + pluginId;
	}

	public static String getPluginWriteAuthority(String pluginId)
	{
		return AUTHORITY_PLUGIN_WRITE_PREFIX + pluginId;
	}

	/**
	 * Get all possible authorities (roles) for an entity
	 */
	public static List<String> getEntityAuthorities(String entityTypeId)
	{
		List<String> authorities = new ArrayList<>();
		for (Permission permission : Permission.values())
		{
			String authority = String.format("%s%s_%s", AUTHORITY_ENTITY_PREFIX, permission.name(), entityTypeId);
			authorities.add(authority);
		}

		return authorities;
	}

	/**
	 * Checks if client session is expired (by checking the requested sessionId).
	 *
	 * @param request
	 * @return true if session is expired
	 */
	public static boolean isSessionExpired(HttpServletRequest request)
	{
		return request.getRequestedSessionId() != null && !request.isRequestedSessionIdValid();

	}
}
