package org.molgenis.security.permission;

import java.util.List;

import org.molgenis.auth.MolgenisUser;
import org.molgenis.auth.UserAuthority;
import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component
public class PermissionSystemService
{
	private final DataService dataService;

	@Autowired
	public PermissionSystemService(DataService dataService)
	{
		this.dataService = dataService;
	}

	@RunAsSystem
	public void giveUserEntityPermissions(SecurityContext securityContext, List<String> entities)
	{
		Authentication auth = securityContext.getAuthentication();

		if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))
				&& !auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SYSTEM")))
		{
			MolgenisUser user = dataService.findOne(MolgenisUser.ENTITY_NAME,
					new QueryImpl().eq(MolgenisUser.USERNAME, SecurityUtils.getUsername(auth)), MolgenisUser.class);

			if (user != null)
			{
				List<GrantedAuthority> roles = Lists.newArrayList(auth.getAuthorities());

				for (String entity : entities)
				{
					for (Permission permission : Permission.values())
					{
						String role = SecurityUtils.AUTHORITY_ENTITY_PREFIX + permission.toString() + "_"
								+ entity.toUpperCase();
						roles.add(new SimpleGrantedAuthority(role));
						UserAuthority userAuthority = new UserAuthority();
						userAuthority.setMolgenisUser(user);
						userAuthority.setRole(role);
						dataService.add(UserAuthority.ENTITY_NAME, userAuthority);
					}
				}

				auth = new UsernamePasswordAuthenticationToken(auth.getPrincipal(), null, roles);
				securityContext.setAuthentication(auth);
			}
		}
	}
}
