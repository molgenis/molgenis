package org.molgenis.security;

import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;

import static org.molgenis.security.core.utils.SecurityUtils.*;

public class MolgenisRoleHierarchy implements RoleHierarchy
{
	@Override
	public Collection<? extends GrantedAuthority> getReachableGrantedAuthorities(
			Collection<? extends GrantedAuthority> authorities)
	{
		Collection<GrantedAuthority> hierarchicalAuthorities = new ArrayList<>();
		for (GrantedAuthority authority : authorities)
		{
			if (authority.getAuthority().startsWith(AUTHORITY_ENTITY_WRITEMETA_PREFIX))
			{
				String entity = authority.getAuthority().substring(AUTHORITY_ENTITY_WRITEMETA_PREFIX.length());
				hierarchicalAuthorities.add(new SimpleGrantedAuthority(AUTHORITY_ENTITY_WRITE_PREFIX + entity));
				hierarchicalAuthorities.add(new SimpleGrantedAuthority(AUTHORITY_ENTITY_READ_PREFIX + entity));
				hierarchicalAuthorities.add(new SimpleGrantedAuthority(AUTHORITY_ENTITY_COUNT_PREFIX + entity));
			}
			else if (authority.getAuthority().startsWith(AUTHORITY_ENTITY_WRITE_PREFIX))
			{
				String entity = authority.getAuthority().substring(AUTHORITY_ENTITY_WRITE_PREFIX.length());
				hierarchicalAuthorities.add(new SimpleGrantedAuthority(AUTHORITY_ENTITY_READ_PREFIX + entity));
				hierarchicalAuthorities.add(new SimpleGrantedAuthority(AUTHORITY_ENTITY_COUNT_PREFIX + entity));
			}
			else if (authority.getAuthority().startsWith(AUTHORITY_ENTITY_READ_PREFIX))
			{
				String entity = authority.getAuthority().substring(AUTHORITY_ENTITY_READ_PREFIX.length());
				hierarchicalAuthorities.add(new SimpleGrantedAuthority(AUTHORITY_ENTITY_COUNT_PREFIX + entity));
			}
			else if (authority.getAuthority().startsWith(AUTHORITY_PLUGIN_WRITEMETA_PREFIX))
			{
				String entity = authority.getAuthority().substring(AUTHORITY_PLUGIN_WRITEMETA_PREFIX.length());
				hierarchicalAuthorities.add(new SimpleGrantedAuthority(AUTHORITY_PLUGIN_WRITE_PREFIX + entity));
				hierarchicalAuthorities.add(new SimpleGrantedAuthority(AUTHORITY_PLUGIN_READ_PREFIX + entity));
				hierarchicalAuthorities.add(new SimpleGrantedAuthority(AUTHORITY_PLUGIN_COUNT_PREFIX + entity));
			}
			else if (authority.getAuthority().startsWith(AUTHORITY_PLUGIN_WRITE_PREFIX))
			{
				String entity = authority.getAuthority().substring(AUTHORITY_PLUGIN_WRITE_PREFIX.length());
				hierarchicalAuthorities.add(new SimpleGrantedAuthority(AUTHORITY_PLUGIN_READ_PREFIX + entity));
				hierarchicalAuthorities.add(new SimpleGrantedAuthority(AUTHORITY_PLUGIN_COUNT_PREFIX + entity));
			}
			else if (authority.getAuthority().startsWith(AUTHORITY_PLUGIN_READ_PREFIX))
			{
				String entity = authority.getAuthority().substring(AUTHORITY_PLUGIN_READ_PREFIX.length());
				hierarchicalAuthorities.add(new SimpleGrantedAuthority(AUTHORITY_PLUGIN_COUNT_PREFIX + entity));
			}

			hierarchicalAuthorities.add(authority);
		}
		return hierarchicalAuthorities;
	}
}
