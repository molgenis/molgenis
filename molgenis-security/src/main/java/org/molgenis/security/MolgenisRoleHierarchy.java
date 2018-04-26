package org.molgenis.security;

import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;

public class MolgenisRoleHierarchy implements RoleHierarchy
{
	@Override
	public Collection<? extends GrantedAuthority> getReachableGrantedAuthorities(
			Collection<? extends GrantedAuthority> authorities)
	{
		Collection<GrantedAuthority> hierarchicalAuthorities = new ArrayList<>();
		for (GrantedAuthority authority : authorities)
		{
			if (authority.getAuthority().equals(SecurityUtils.AUTHORITY_SU))
			{
				hierarchicalAuthorities.add(new SimpleGrantedAuthority(SecurityUtils.ROLE_ACL_TAKE_OWNERSHIP));
				hierarchicalAuthorities.add(new SimpleGrantedAuthority(SecurityUtils.ROLE_ACL_MODIFY_AUDITING));
				hierarchicalAuthorities.add(new SimpleGrantedAuthority(SecurityUtils.ROLE_ACL_GENERAL_CHANGES));
			}

			hierarchicalAuthorities.add(authority);
		}
		return hierarchicalAuthorities;
	}
}
