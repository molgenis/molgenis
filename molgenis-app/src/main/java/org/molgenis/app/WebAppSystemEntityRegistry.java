package org.molgenis.app;

import org.molgenis.app.controller.HomeController;
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
import static org.molgenis.auth.UserMetaData.USER;
import static org.molgenis.auth.UserMetaData.USERNAME;
import static org.molgenis.security.account.AccountService.ALL_USER_GROUP;
import static org.molgenis.security.core.utils.SecurityUtils.ANONYMOUS_USERNAME;
import static org.molgenis.security.core.utils.SecurityUtils.AUTHORITY_PLUGIN_READ_PREFIX;

/**
 * Registry of application system entities to be added to an empty database.
 */
@Component
public class WebAppSystemEntityRegistry implements SystemEntityRegistry
{
	private final DataService dataService;
	private final UserAuthorityFactory userAuthorityFactory;
	private final GroupAuthorityFactory groupAuthorityFactory;

	@Autowired
	public WebAppSystemEntityRegistry(DataService dataService, UserAuthorityFactory userAuthorityFactory,
			GroupAuthorityFactory groupAuthorityFactory)
	{
		this.dataService = requireNonNull(dataService);
		this.userAuthorityFactory = requireNonNull(userAuthorityFactory);
		this.groupAuthorityFactory = requireNonNull(groupAuthorityFactory);
	}

	@Override
	public Collection<Entity> getEntities()
	{
		User anonymousUser = dataService.query(USER, User.class).eq(USERNAME, ANONYMOUS_USERNAME).findOne();

		// allow anonymous user to see the home plugin
		UserAuthority anonymousHomeAuthority = userAuthorityFactory.create();
		anonymousHomeAuthority.setUser(anonymousUser);
		anonymousHomeAuthority.setRole(AUTHORITY_PLUGIN_READ_PREFIX + HomeController.ID);

		Group allUsersGroup = dataService.query(GROUP, Group.class).eq(NAME, ALL_USER_GROUP).findOne();

		// allow users in the all users group to see the home plugin
		GroupAuthority usersGroupHomeAuthority = groupAuthorityFactory.create();
		usersGroupHomeAuthority.setGroup(allUsersGroup);
		usersGroupHomeAuthority.setRole(AUTHORITY_PLUGIN_READ_PREFIX + HomeController.ID);

		return asList(anonymousHomeAuthority, usersGroupHomeAuthority);
	}
}
