package org.molgenis.data.security.permission;

import org.molgenis.data.DataService;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.security.auth.*;
import org.molgenis.data.security.user.UserService;
import org.springframework.stereotype.Component;

import java.time.Instant;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.security.auth.RoleMembershipMetadata.ROLE_MEMBERSHIP;
import static org.molgenis.data.security.auth.RoleMetadata.NAME;
import static org.molgenis.data.security.auth.UserMetaData.USERNAME;

@Component
public class RoleMembershipServiceImpl implements RoleMembershipService
{
	private final UserService userService;
	private final RoleMembershipFactory roleMembershipFactory;
	private final DataService dataService;
	private final UserMetaData userMetaData;
	private final RoleMetadata roleMetadata;

	RoleMembershipServiceImpl(UserService userService, RoleMembershipFactory roleMembershipFactory,
			DataService dataService, UserMetaData userMetaData, RoleMetadata roleMetadata)
	{
		this.userService = requireNonNull(userService);
		this.roleMembershipFactory = requireNonNull(roleMembershipFactory);
		this.dataService = requireNonNull(dataService);
		this.userMetaData = requireNonNull(userMetaData);
		this.roleMetadata = requireNonNull(roleMetadata);
	}

	@Override
	public void addUserToRole(String username, String rolename)
	{
		User groupCreator = userService.getUser(username);
		if (groupCreator == null)
		{
			throw new UnknownEntityException(userMetaData, userMetaData.getAttribute(USERNAME), username);
		}

		Role role = dataService.query(RoleMetadata.ROLE, Role.class).eq(NAME, rolename).findOne();
		if (role == null)
		{
			throw new UnknownEntityException(roleMetadata, roleMetadata.getAttribute(NAME), rolename);
		}

		RoleMembership roleMembership = roleMembershipFactory.create();
		roleMembership.setUser(groupCreator);
		roleMembership.setFrom(Instant.now());
		roleMembership.setRole(role);

		dataService.add(ROLE_MEMBERSHIP, roleMembership);
	}
}
