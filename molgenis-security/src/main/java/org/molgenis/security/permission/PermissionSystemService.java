package org.molgenis.security.permission;

import com.google.common.collect.Lists;
import org.molgenis.auth.User;
import org.molgenis.auth.UserAuthority;
import org.molgenis.auth.UserAuthorityFactory;
import org.molgenis.auth.UserMetaData;
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

import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.molgenis.auth.UserAuthorityMetaData.USER_AUTHORITY;
import static org.molgenis.auth.UserMetaData.USER;

@Component
public class PermissionSystemService
{
	private final DataService dataService;
	private final UserAuthorityFactory userAuthorityFactory;

	@Autowired
	public PermissionSystemService(DataService dataService, UserAuthorityFactory userAuthorityFactory)
	{
		this.dataService = requireNonNull(dataService);
		this.userAuthorityFactory = requireNonNull(userAuthorityFactory);
	}

	@RunAsSystem
	public void giveUserEntityPermissions(SecurityContext securityContext, List<String> entities)
	{
		Authentication auth = securityContext.getAuthentication();

		if (!auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")) && !auth.getAuthorities()
				.contains(new SimpleGrantedAuthority("ROLE_SYSTEM")))
		{
			User user = dataService.findOne(USER,
					new QueryImpl<User>().eq(UserMetaData.USERNAME, SecurityUtils.getUsername(auth)),
					User.class);

			if (user != null)
			{
				List<GrantedAuthority> roles = Lists.newArrayList(auth.getAuthorities());

				for (String entity : entities)
				{
					for (Permission permission : Permission.values())
					{
						if (permission != Permission.NONE)
						{
							String role = SecurityUtils.AUTHORITY_ENTITY_PREFIX + permission.toString() + "_" + entity;
							roles.add(new SimpleGrantedAuthority(role));
							UserAuthority userAuthority = userAuthorityFactory.create();
							userAuthority.setUser(user);
							userAuthority.setRole(role);

							if (permission == Permission.WRITEMETA)
							{
								dataService.add(USER_AUTHORITY, userAuthority);
							}
						}
					}
				}

				auth = new UsernamePasswordAuthenticationToken(auth.getPrincipal(), null, roles);
				securityContext.setAuthentication(auth);
			}
		}
	}
}
