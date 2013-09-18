package org.molgenis.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.GrantedAuthority;

public class MolgenisRoleHierarchy implements RoleHierarchy
{

	@Override
	public Collection<? extends GrantedAuthority> getReachableGrantedAuthorities(
			Collection<? extends GrantedAuthority> authorities)
	{
		List<? extends GrantedAuthority> allAuthorities = new ArrayList<GrantedAuthority>(authorities);
		for (GrantedAuthority authority : authorities)
		{
			System.out.println(authority.getAuthority());
		}
		return allAuthorities;
	}

}
