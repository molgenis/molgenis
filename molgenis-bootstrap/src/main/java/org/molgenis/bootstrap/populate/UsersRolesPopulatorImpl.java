package org.molgenis.bootstrap.populate;

import org.molgenis.data.DataService;
import org.molgenis.data.security.auth.Role;
import org.molgenis.data.security.auth.RoleFactory;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.auth.UserFactory;
import org.molgenis.security.core.runas.RunAsSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.security.auth.RoleMetadata.ROLE;
import static org.molgenis.data.security.auth.UserMetaData.USER;
import static org.molgenis.security.account.AccountService.ROLE_USER;
import static org.molgenis.security.core.utils.SecurityUtils.ANONYMOUS_USERNAME;

@Service
public class UsersRolesPopulatorImpl implements UsersRolesPopulator
{
	private static final Logger LOG = LoggerFactory.getLogger(UsersRolesPopulatorImpl.class);

	private static final String USERNAME_ADMIN = "admin";

	private final DataService dataService;
	private final UserFactory userFactory;
	private final RoleFactory roleFactory;

	@Value("${admin.password:@null}")
	private String adminPassword;
	@Value("${admin.email:molgenis+admin@gmail.com}")
	private String adminEmail;
	@Value("${anonymous.email:molgenis+anonymous@gmail.com}")
	private String anonymousEmail;

	UsersRolesPopulatorImpl(DataService dataService, UserFactory userFactory, RoleFactory roleFactory)
	{
		this.dataService = requireNonNull(dataService);
		this.userFactory = requireNonNull(userFactory);
		this.roleFactory = requireNonNull(roleFactory);
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

		// create user role
		Role userRole = roleFactory.create();
		userRole.setName(ROLE_USER);
		userRole.setLabel("User");
		userRole.setLabel("en", "User");
		userRole.setLabel("nl", "Gebruiker");
		userRole.setDescription("All authenticated users are a member of this Role.");
		userRole.setDescription("en", "All authenticated users are a member of this role.");
		userRole.setDescription("nl", "Alle geauthenticeerde gebruikers hebben deze rol.");

		// persist entities
		dataService.add(USER, Stream.of(userAdmin, anonymousUser));
		dataService.add(ROLE, userRole);
	}
}