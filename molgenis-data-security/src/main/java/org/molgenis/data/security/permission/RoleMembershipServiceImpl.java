package org.molgenis.data.security.permission;

import org.molgenis.data.DataService;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.security.auth.*;
import org.molgenis.data.security.user.UserService;
import org.springframework.stereotype.Component;

import java.time.Instant;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.security.auth.RoleMembershipMetadata.ROLE_MEMBERSHIP;

@Component
public class RoleMembershipServiceImpl implements RoleMembershipService
{
	private final UserService userService;
	private final RoleMembershipFactory roleMembershipFactory;
	private final DataService dataService;

	RoleMembershipServiceImpl(UserService userService, RoleMembershipFactory roleMembershipFactory,
			DataService dataService)
	{
		this.userService = requireNonNull(userService);
		this.roleMembershipFactory = requireNonNull(roleMembershipFactory);
		this.dataService = requireNonNull(dataService);
	}

	@Override
	public void addUserToRole(String username, String rolename)
	{
		User groupCreator = userService.getUser(username);
		if (groupCreator == null)
		{
			throw new UnknownEntityException(format("User with name [%s] not found", username));
		}

		Role role = dataService.query(RoleMetadata.ROLE, Role.class).eq(RoleMetadata.NAME, rolename).findOne();
		if (role == null)
		{
			throw new UnknownEntityException(format("Role with name [%s] not found", rolename));
		}

		RoleMembership roleMembership = roleMembershipFactory.create();
		roleMembership.setUser(groupCreator);
		roleMembership.setFrom(Instant.now());
		roleMembership.setRole(role);

		dataService.add(ROLE_MEMBERSHIP, roleMembership);
	}
}
