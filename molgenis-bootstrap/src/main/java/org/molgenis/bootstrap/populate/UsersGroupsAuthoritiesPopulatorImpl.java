package org.molgenis.bootstrap.populate;

import org.molgenis.auth.*;
import org.molgenis.data.DataService;
import org.molgenis.security.account.AccountService;
import org.molgenis.security.core.runas.RunAsSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.molgenis.auth.GroupAuthorityMetaData.GROUP_AUTHORITY;
import static org.molgenis.auth.GroupMetaData.GROUP;
import static org.molgenis.auth.RoleMetadata.ROLE;
import static org.molgenis.auth.UserMetaData.USER;
import static org.molgenis.security.core.utils.SecurityUtils.ANONYMOUS_USERNAME;

@Service
public class UsersGroupsAuthoritiesPopulatorImpl implements UsersGroupsAuthoritiesPopulator
{
	private static final String USERNAME_ADMIN = "admin";
	public static final String ROLE_USER_ID = "user";
	private static final String ROLE_USER_LABEL = "User";

	private final DataService dataService;
	private final UserFactory userFactory;
	private final GroupFactory groupFactory;
	private final GroupAuthorityFactory groupAuthorityFactory;
	private final RoleFactory roleFactory;

	@Value("${admin.password:@null}")
	private String adminPassword;
	@Value("${admin.email:molgenis+admin@gmail.com}")
	private String adminEmail;
	@Value("${anonymous.email:molgenis+anonymous@gmail.com}")
	private String anonymousEmail;

	@Autowired
	UsersGroupsAuthoritiesPopulatorImpl(DataService dataService, UserFactory userFactory, GroupFactory groupFactory,
			GroupAuthorityFactory groupAuthorityFactory, RoleFactory roleFactory)
	{
		this.dataService = requireNonNull(dataService);
		this.userFactory = requireNonNull(userFactory);
		this.groupFactory = requireNonNull(groupFactory);
		this.groupAuthorityFactory = requireNonNull(groupAuthorityFactory);
		this.roleFactory = requireNonNull(roleFactory);
	}

	@Override
	@Transactional
	@RunAsSystem
	public void populate()
	{
		if (adminPassword == null)
		{
			throw new RuntimeException(
					"please configure the admin.password property in your molgenis-server.properties");
		}

		// create admin user
		User userAdmin = userFactory.create();
		userAdmin.setUsername(USERNAME_ADMIN);
		userAdmin.setPassword(adminPassword);
		userAdmin.setEmail(adminEmail);
		userAdmin.setActive(true);
		userAdmin.setSuperuser(true);
		userAdmin.setChangePassword(false);

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

		// create user role
		Role userRole = roleFactory.create(ROLE_USER_ID);
		userRole.setLabel(ROLE_USER_LABEL);

		// assign group authority for all users group with user role
		GroupAuthority usersGroupUserAuthority = groupAuthorityFactory.create();
		usersGroupUserAuthority.setGroup(allUsersGroup);
		usersGroupUserAuthority.setRole(userRole);

		// persist entities
		dataService.add(USER, Stream.of(userAdmin, anonymousUser));
		dataService.add(GROUP, allUsersGroup);
		dataService.add(ROLE, userRole);
		dataService.add(GROUP_AUTHORITY, usersGroupUserAuthority);
	}
}