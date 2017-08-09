package org.molgenis.security.permission;

import com.google.common.collect.Lists;
import org.molgenis.auth.User;
import org.molgenis.auth.UserAuthority;
import org.molgenis.auth.UserAuthorityFactory;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.singleton;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.auth.UserAuthorityMetaData.USER_AUTHORITY;
import static org.molgenis.security.core.Permission.WRITEMETA;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_ENTITY_PREFIX;

@Component
public class PermissionSystemServiceImpl implements PermissionSystemService
{
	private final UserService userService;
	private final UserAuthorityFactory userAuthorityFactory;
	private final RoleHierarchy roleHierarchy;
	private final DataService dataService;

	@Autowired
	public PermissionSystemServiceImpl(UserService userService, UserAuthorityFactory userAuthorityFactory,
			RoleHierarchy roleHierarchy, DataService dataService)
	{
		this.userService = requireNonNull(userService);
		this.userAuthorityFactory = requireNonNull(userAuthorityFactory);
		this.roleHierarchy = requireNonNull(roleHierarchy);
		this.dataService = requireNonNull(dataService);
	}

	@Override
	public void giveUserWriteMetaPermissions(EntityType entityType)
	{
		giveUserWriteMetaPermissions(singleton(entityType));
	}

	@Override
	public void giveUserWriteMetaPermissions(Collection<EntityType> entityTypes)
	{
		// superusers and system user have all permissions by default
		if (SecurityUtils.currentUserIsSuOrSystem())
		{
			return;
		}

		SecurityContext securityContext = SecurityContextHolder.getContext();
		runAsSystem(() -> giveUserEntityPermissionsAsSystem(securityContext, entityTypes));
	}

	private void giveUserEntityPermissionsAsSystem(SecurityContext securityContext, Collection<EntityType> entityTypes)
	{
		Collection<GrantedAuthority> grantedAuthorities = getGrantedAuthorities(entityTypes);
		updateUserAuthorities(securityContext, grantedAuthorities);
		updateSecurityContext(securityContext, grantedAuthorities);
	}

	private Collection<GrantedAuthority> getGrantedAuthorities(Collection<EntityType> entityTypeStream)
	{
		return entityTypeStream.stream().map(this::toGrantedAuthority).collect(toList());
	}

	private GrantedAuthority toGrantedAuthority(EntityType entityType)
	{
		@SuppressWarnings("StringConcatenationMissingWhitespace")
		String role = AUTHORITY_ENTITY_PREFIX + WRITEMETA.toString() + '_' + entityType.getId();
		return new SimpleGrantedAuthority(role);
	}

	private void updateUserAuthorities(SecurityContext context, Collection<GrantedAuthority> grantedAuthorities)
	{
		User user = userService.getUser(SecurityUtils.getUsername(context.getAuthentication()));
		Stream<UserAuthority> userAuthorityStream = grantedAuthorities.stream().map(grantedAuthority ->
		{
			UserAuthority userAuthority = userAuthorityFactory.create();
			userAuthority.setUser(user);
			userAuthority.setRole(grantedAuthority.getAuthority());
			return userAuthority;
		});
		dataService.add(USER_AUTHORITY, userAuthorityStream);
	}

	private void updateSecurityContext(SecurityContext context, Collection<? extends GrantedAuthority> authorities)
	{
		Collection<? extends GrantedAuthority> reachableAuthorities = roleHierarchy.getReachableGrantedAuthorities(
				authorities);

		List<GrantedAuthority> newGrantedAuthorities = Lists.newArrayList(context.getAuthentication().getAuthorities());
		newGrantedAuthorities.addAll(reachableAuthorities);

		Authentication authentication = context.getAuthentication();
		Object principal = authentication.getPrincipal();
		Object credentials = authentication.getCredentials();
		context.setAuthentication(
				new UsernamePasswordAuthenticationToken(principal, credentials, newGrantedAuthorities));
	}

}
