package org.molgenis.omx;

import java.util.ArrayList;
import java.util.Collection;

import org.molgenis.framework.server.MolgenisPermissionService.Permission;
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
			String[] tokens = authority.getAuthority().split("_");
			if (tokens.length == 4)
			{
				String mode = tokens[2];
				if (mode.equals(Permission.WRITE.toString()))
				{
					String readRole = String.format("%s_%s_%s_%s", tokens[0], tokens[1], Permission.READ.toString(),
							tokens[3]);
					hierarchicalAuthorities.add(new SimpleGrantedAuthority(readRole));
					String countRole = String.format("%s_%s_%s_%s", tokens[0], tokens[1], Permission.COUNT.toString(),
							tokens[3]);
					hierarchicalAuthorities.add(new SimpleGrantedAuthority(countRole));
				}
				else if (mode.equals(Permission.READ.toString()))
				{
					String countRole = String.format("%s_%s_%s_%s", tokens[0], tokens[1], Permission.COUNT.toString(),
							tokens[3]);
					hierarchicalAuthorities.add(new SimpleGrantedAuthority(countRole));
				}
			}
			hierarchicalAuthorities.add(authority);
		}
		return hierarchicalAuthorities;
	}
}
