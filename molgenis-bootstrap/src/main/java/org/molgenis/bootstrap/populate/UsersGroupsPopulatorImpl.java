package org.molgenis.bootstrap.populate;

import org.molgenis.data.DataService;
import org.molgenis.data.security.auth.Group;
import org.molgenis.data.security.auth.GroupFactory;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.auth.UserFactory;
import org.molgenis.security.account.AccountService;
import org.molgenis.security.core.runas.RunAsSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.security.auth.GroupMetaData.GROUP;
import static org.molgenis.data.security.auth.UserMetaData.USER;
import static org.molgenis.security.core.utils.SecurityUtils.ANONYMOUS_USERNAME;

@Service
public class UsersGroupsPopulatorImpl implements UsersGroupsPopulator
{
	private static final Logger LOG = LoggerFactory.getLogger(UsersGroupsPopulatorImpl.class);

	private static final String USERNAME_ADMIN = "admin";

	private final DataService dataService;
	private final UserFactory userFactory;
	private final GroupFactory groupFactory;

	@Value("${admin.password:@null}")
	private String adminPassword;
	@Value("${admin.email:molgenis+admin@gmail.com}")
	private String adminEmail;
	@Value("${anonymous.email:molgenis+anonymous@gmail.com}")
	private String anonymousEmail;

	UsersGroupsPopulatorImpl(DataService dataService, UserFactory userFactory, GroupFactory groupFactory)
	{
		this.dataService = requireNonNull(dataService);
		this.userFactory = requireNonNull(userFactory);
		this.groupFactory = requireNonNull(groupFactory);
	}

	@Override
	@Transactional
	@RunAsSystem
	public void populate()
	{
		boolean changeAdminPassword = false;
		if (adminPassword == null)
		{
			adminPassword = UUID.randomUUID().toString();
			changeAdminPassword = true;
			LOG.info("Password for user 'admin': {}", adminPassword);
		}

		// create admin user
		User userAdmin = userFactory.create();
		userAdmin.setUsername(USERNAME_ADMIN);
		userAdmin.setPassword(adminPassword);
		userAdmin.setEmail(adminEmail);
		userAdmin.setActive(true);
		userAdmin.setSuperuser(true);
		userAdmin.setChangePassword(changeAdminPassword);

		// create anonymous user
		User anonymousUser = userFactory.create();
		anonymousUser.setUsername(ANONYMOUS_USERNAME);
		anonymousUser.setPassword(ANONYMOUS_USERNAME);
		anonymousUser.setEmail(anonymousEmail);
		anonymousUser.setActive(true);
		anonymousUser.setSuperuser(false);
		anonymousUser.setChangePassword(false);

		// create all users group
		Group allUsersGroup = groupFactory.create();
		allUsersGroup.setName(AccountService.ALL_USER_GROUP);

		// persist entities
		dataService.add(USER, Stream.of(userAdmin, anonymousUser));
		dataService.add(GROUP, allUsersGroup);
	}
}