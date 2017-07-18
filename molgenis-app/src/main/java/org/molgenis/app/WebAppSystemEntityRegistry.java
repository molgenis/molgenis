package org.molgenis.app;

import org.molgenis.auth.*;
import org.molgenis.bootstrap.populate.SystemEntityRegistry;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static org.molgenis.auth.GroupMetaData.GROUP;
import static org.molgenis.auth.GroupMetaData.NAME;
import static org.molgenis.security.account.AccountService.ALL_USER_GROUP;

/**
 * Registry of application system entities to be added to an empty database.
 */
@Component
public class WebAppSystemEntityRegistry implements SystemEntityRegistry
{
	private final DataService dataService;
	private final GroupAuthorityFactory groupAuthorityFactory;
	private final RoleFactory roleFactory;

	@Autowired
	public WebAppSystemEntityRegistry(DataService dataService, GroupAuthorityFactory groupAuthorityFactory,
			RoleFactory roleFactory)
	{
		this.dataService = requireNonNull(dataService);
		this.groupAuthorityFactory = requireNonNull(groupAuthorityFactory);
		this.roleFactory = requireNonNull(roleFactory);
	}

	@Override
	public Collection<Entity> getEntities()
	{
		Role userRole = roleFactory.create();
		userRole.setLabel("User");

		Group allUsersGroup = dataService.query(GROUP, Group.class).eq(NAME, ALL_USER_GROUP).findOne();

		// assign 'User' role to 'All Users' group
		GroupAuthority usersGroupUserAuthority = groupAuthorityFactory.create();
		usersGroupUserAuthority.setGroup(allUsersGroup);
		usersGroupUserAuthority.setRole(userRole);

		return asList(userRole, usersGroupUserAuthority);
	}
}
