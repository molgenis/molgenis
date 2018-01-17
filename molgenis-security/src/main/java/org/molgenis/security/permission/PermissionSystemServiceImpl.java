package org.molgenis.security.permission;

import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.auth.UserAuthority;
import org.molgenis.data.security.auth.UserAuthorityFactory;
import org.molgenis.data.security.permission.PermissionSystemService;
import org.molgenis.data.security.user.UserService;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.singleton;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.molgenis.data.security.auth.UserAuthorityMetaData.USER_AUTHORITY;
import static org.molgenis.security.core.Permission.WRITEMETA;
import static org.molgenis.security.core.runas.RunAsSystemAspect.runAsSystem;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_ENTITY_PREFIX;

@Component
public class PermissionSystemServiceImpl implements PermissionSystemService
{
	private final UserService userService;
	private final UserAuthorityFactory userAuthorityFactory;
	private final RoleHierarchy roleHierarchy;
	private final DataService dataService;
	private final PrincipalSecurityContextRegistry principalSecurityContextRegistry;
	private final AuthenticationAuthoritiesUpdater authenticationAuthoritiesUpdater;

	public PermissionSystemServiceImpl(UserService userService, UserAuthorityFactory userAuthorityFactory,
			RoleHierarchy roleHierarchy, DataService dataService,
			PrincipalSecurityContextRegistry principalSecurityContextRegistry,
			AuthenticationAuthoritiesUpdater authenticationAuthoritiesUpdater)
	{
		this.userService = requireNonNull(userService);
		this.userAuthorityFactory = requireNonNull(userAuthorityFactory);
		this.roleHierarchy = requireNonNull(roleHierarchy);
		this.dataService = requireNonNull(dataService);
		this.principalSecurityContextRegistry = requireNonNull(principalSecurityContextRegistry);
		this.authenticationAuthoritiesUpdater = requireNonNull(authenticationAuthoritiesUpdater);
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

		Collection<GrantedAuthority> grantedAuthorities = getGrantedAuthorities(entityTypes);
		giveUserEntityPermissionsAsSystem(grantedAuthorities);
	}

	private void giveUserEntityPermissionsAsSystem(Collection<GrantedAuthority> authorities)
	{
		String currentUsername = SecurityUtils.getCurrentUsername();
		runAsSystem(() -> updatePersistedUserAuthorities(currentUsername, authorities));

		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Stream<SecurityContext> securityContexts = principalSecurityContextRegistry.getSecurityContexts(principal);

		Collection<? extends GrantedAuthority> reachableAuthorities = roleHierarchy.getReachableGrantedAuthorities(
				authorities);
		securityContexts.forEach(
				securityContext -> updateSecurityContextAuthorities(securityContext, reachableAuthorities));
	}

	private void updateSecurityContextAuthorities(SecurityContext securityContext,
			Collection<? extends GrantedAuthority> newAuthorities)
	{
		Collection<? extends GrantedAuthority> authorities = securityContext.getAuthentication().getAuthorities();
		List<GrantedAuthority> updatedAuthorities = new ArrayList<>(authorities);
		updatedAuthorities.addAll(newAuthorities);

		Authentication updatedAuthentication = authenticationAuthoritiesUpdater.updateAuthentication(
				securityContext.getAuthentication(), updatedAuthorities);
		securityContext.setAuthentication(updatedAuthentication);
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

	private void updatePersistedUserAuthorities(String currentUsername, Collection<GrantedAuthority> grantedAuthorities)
	{
		User user = userService.getUser(currentUsername);
		Stream<UserAuthority> userAuthorityStream = grantedAuthorities.stream().map(grantedAuthority ->
		{
			UserAuthority userAuthority = userAuthorityFactory.create();
			userAuthority.setUser(user);
			userAuthority.setRole(grantedAuthority.getAuthority());
			return userAuthority;
		});
		dataService.add(USER_AUTHORITY, userAuthorityStream);
	}
}
