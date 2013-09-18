package org.molgenis.security;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

public class SecurityUtils
{
	public static final String AUTHORITY_PLUGIN_PREFIX = "ROLE_PLUGIN_";
	public static final String AUTHORITY_PLUGIN_READ_PREFIX = "ROLE_PLUGIN_READ_";
	public static final String AUTHORITY_PLUGIN_WRITE_PREFIX = "ROLE_PLUGIN_WRITE_";
	public static final String AUTHORITY_ENTITY_PREFIX = "ROLE_ENTITY_";
	public static final String AUTHORITY_ENTITY_READ_PREFIX = "ROLE_ENTITY_READ_";
	public static final String AUTHORITY_ENTITY_WRITE_PREFIX = "ROLE_ENTITY_WRITE_";

	public static final GrantedAuthority GRANTED_AUTHORITY_SU = new SimpleGrantedAuthority("ROLE_SU");

	public static UserDetails getCurrentUser()
	{
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null)
		{
			return null;
		}

		Object principal = authentication.getPrincipal();
		if (!(principal instanceof UserDetails))
		{
			return new User(principal.toString(), "", Collections.<GrantedAuthority> emptyList());
		}
		return (UserDetails) principal;
	}

	public static String getCurrentUsername()
	{
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Object principal = authentication.getPrincipal();
		if (!(principal instanceof UserDetails)) return ((UserDetails) principal).getUsername();
		else return principal.toString();
	}

	public static boolean isUserInRole(String... roles)
	{
		Collection<? extends GrantedAuthority> authorities = getCurrentUser().getAuthorities();
		for (String role : roles)
		{
			if (authorities.contains(new SimpleGrantedAuthority(role))) return true;
		}
		return false;
	}

	public static boolean currentUserIsSu()
	{
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null) return false;

		Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
		if (authorities == null) throw new IllegalStateException("No current user logged in");

		for (GrantedAuthority authority : authorities)
		{
			if (authority.getAuthority().equals(GRANTED_AUTHORITY_SU.getAuthority())) return true;
		}

		return false;
	}

	public static boolean currentUserIsAuthenticated()
	{
		String username;
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Object principal = authentication.getPrincipal();
		if (principal instanceof UserDetails) username = ((UserDetails) principal).getUsername();
		else username = principal.toString();
		return authentication.isAuthenticated() && !username.equals("anonymousUser");
	}

	public static String[] defaultPluginAuthorities(String pluginId)
	{
		return new String[]
		{ GRANTED_AUTHORITY_SU.getAuthority(), getPluginReadAuthority(pluginId), getPluginWriteAuthority(pluginId) };
	}

	private static String getPluginReadAuthority(String pluginId)
	{
		return AUTHORITY_PLUGIN_READ_PREFIX + pluginId.toUpperCase();
	}

	private static String getPluginWriteAuthority(String pluginId)
	{
		return AUTHORITY_PLUGIN_WRITE_PREFIX + pluginId.toUpperCase();
	}
}
