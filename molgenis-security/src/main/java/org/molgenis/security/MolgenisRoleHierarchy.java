package org.molgenis.security;

import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_ENTITY_COUNT_PREFIX;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_ENTITY_READ_PREFIX;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_ENTITY_WRITE_PREFIX;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_PLUGIN_COUNT_PREFIX;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_PLUGIN_READ_PREFIX;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_PLUGIN_WRITE_PREFIX;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class MolgenisRoleHierarchy implements RoleHierarchy
{
	@Override
	public Collection<? extends GrantedAuthority> getReachableGrantedAuthorities(
			Collection<? extends GrantedAuthority> authorities)
	{
		Collection<GrantedAuthority> hierarchicalAuthorities = new ArrayList<GrantedAuthority>();
		for (GrantedAuthority authority : authorities)
		{
			if (authority.getAuthority().startsWith(AUTHORITY_ENTITY_WRITE_PREFIX))
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

			if (authority.getAuthority().equals(AUTHORITY_PLUGIN_READ_PREFIX + "IMPORTWIZARD")
					|| authority.getAuthority().equals(AUTHORITY_PLUGIN_WRITE_PREFIX + "IMPORTWIZARD"))
			{
				hierarchicalAuthorities
						.add(new SimpleGrantedAuthority(AUTHORITY_ENTITY_READ_PREFIX + "RUNTIMEPROPERTY"));
				hierarchicalAuthorities.add(new SimpleGrantedAuthority(AUTHORITY_ENTITY_READ_PREFIX + "IMPORTRUN"));
				hierarchicalAuthorities.add(new SimpleGrantedAuthority(AUTHORITY_ENTITY_WRITE_PREFIX + "IMPORTRUN"));
				hierarchicalAuthorities.add(new SimpleGrantedAuthority(AUTHORITY_ENTITY_COUNT_PREFIX + "IMPORTRUN"));
			}

			hierarchicalAuthorities.add(authority);
		}
		return hierarchicalAuthorities;
	}
}
