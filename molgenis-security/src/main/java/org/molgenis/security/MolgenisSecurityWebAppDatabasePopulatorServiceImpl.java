package org.molgenis.security;

import static java.util.Objects.requireNonNull;
import static org.molgenis.auth.GroupAuthorityMetaData.GROUP_AUTHORITY;
import static org.molgenis.auth.MolgenisGroupMetaData.MOLGENIS_GROUP;
import static org.molgenis.auth.MolgenisUserMetaData.MOLGENIS_USER;
import static org.molgenis.auth.UserAuthorityMetaData.USER_AUTHORITY;
import static org.molgenis.data.i18n.LanguageMetaData.LANGUAGE;

import org.molgenis.auth.GroupAuthority;
import org.molgenis.auth.GroupAuthorityFactory;
import org.molgenis.auth.MolgenisGroup;
import org.molgenis.auth.MolgenisGroupFactory;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.auth.MolgenisUserFactory;
import org.molgenis.auth.UserAuthority;
import org.molgenis.auth.UserAuthorityFactory;
import org.molgenis.data.DataService;
import org.molgenis.security.account.AccountService;
import org.molgenis.security.core.runas.RunAsSystem;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MolgenisSecurityWebAppDatabasePopulatorServiceImpl
		implements MolgenisSecurityWebAppDatabasePopulatorService
{
	private static final String USERNAME_ADMIN = "admin";
	private final MolgenisUserFactory molgenisUserFactory;
	private final MolgenisGroupFactory molgenisGroupFactory;
	private final UserAuthorityFactory userAuthorityFactory;
	private final GroupAuthorityFactory groupAuthorityFactory;

	@Value("${admin.password:@null}")
	private String adminPassword;
	@Value("${admin.email:molgenis+admin@gmail.com}")
	private String adminEmail;
	@Value("${anonymous.email:molgenis+anonymous@gmail.com}")
	private String anonymousEmail;

	private MolgenisUser userAdmin;
	private MolgenisUser anonymousUser;
	private MolgenisGroup allUsersGroup;

	@Autowired
	MolgenisSecurityWebAppDatabasePopulatorServiceImpl(MolgenisUserFactory molgenisUserFactory,
			MolgenisGroupFactory molgenisGroupFactory, UserAuthorityFactory userAuthorityFactory,
			GroupAuthorityFactory groupAuthorityFactory)
	{

		this.molgenisUserFactory = requireNonNull(molgenisUserFactory);
		this.molgenisGroupFactory = molgenisGroupFactory;
		this.userAuthorityFactory = requireNonNull(userAuthorityFactory);
		this.groupAuthorityFactory = requireNonNull(groupAuthorityFactory);
	}

	@Override
	@Transactional
	@RunAsSystem
	public void populateDatabase(DataService dataService, String homeControllerId)
	{
		if (adminPassword == null) throw new RuntimeException(
				"please configure the admin.password property in your molgenis-server.properties");

		// create admin user
		userAdmin = molgenisUserFactory.create();
		userAdmin.setUsername(USERNAME_ADMIN);
		userAdmin.setPassword(adminPassword);
		userAdmin.setEmail(adminEmail);
		userAdmin.setActive(true);
		userAdmin.setSuperuser(true);
		userAdmin.setChangePassword(false);
		dataService.add(MOLGENIS_USER, userAdmin);

		// create anonymous user
		anonymousUser = molgenisUserFactory.create();
		anonymousUser.setUsername(SecurityUtils.ANONYMOUS_USERNAME);
		anonymousUser.setPassword(SecurityUtils.ANONYMOUS_USERNAME);
		anonymousUser.setEmail(anonymousEmail);
		anonymousUser.setActive(true);
		anonymousUser.setSuperuser(false);
		anonymousUser.setChangePassword(false);
		dataService.add(MOLGENIS_USER, anonymousUser);

		// set anonymous role for anonymous user
		UserAuthority anonymousAuthority = userAuthorityFactory.create();
		anonymousAuthority.setMolgenisUser(anonymousUser);
		anonymousAuthority.setRole(SecurityUtils.AUTHORITY_ANONYMOUS);
		dataService.add(USER_AUTHORITY, anonymousAuthority);

		// create all users group
		allUsersGroup = molgenisGroupFactory.create();
		allUsersGroup.setName(AccountService.ALL_USER_GROUP);
		dataService.add(MOLGENIS_GROUP, allUsersGroup);
		dataService.getRepository(MOLGENIS_GROUP).flush();

		// allow all users to see the home plugin
		GroupAuthority usersGroupHomeAuthority = groupAuthorityFactory.create();
		usersGroupHomeAuthority.setMolgenisGroup(allUsersGroup);
		usersGroupHomeAuthority.setRole(SecurityUtils.AUTHORITY_PLUGIN_READ_PREFIX + homeControllerId.toUpperCase());
		dataService.add(GROUP_AUTHORITY, usersGroupHomeAuthority);

		// allow all users to update their profile
		GroupAuthority usersGroupUserAccountAuthority = groupAuthorityFactory.create();
		usersGroupUserAccountAuthority.setMolgenisGroup(allUsersGroup);
		usersGroupUserAccountAuthority
				.setRole(SecurityUtils.AUTHORITY_PLUGIN_WRITE_PREFIX + "useraccount".toUpperCase()); // FIXME do not
		// hardcode
		dataService.add(GROUP_AUTHORITY, usersGroupUserAccountAuthority);

		// allow all users to read the app languages
		GroupAuthority usersGroupLanguagesAuthority = groupAuthorityFactory.create();
		usersGroupLanguagesAuthority.setMolgenisGroup(allUsersGroup);
		usersGroupLanguagesAuthority.setRole(SecurityUtils.AUTHORITY_ENTITY_READ_PREFIX + LANGUAGE.toUpperCase());
		dataService.add(GROUP_AUTHORITY, usersGroupLanguagesAuthority);
	}

	@Override
	public MolgenisUser getAnonymousUser()
	{
		return anonymousUser;
	}
}