package org.molgenis.security;

import org.molgenis.auth.*;
import org.molgenis.data.DataService;
import org.molgenis.security.account.AccountService;
import org.molgenis.security.core.runas.RunAsSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static org.molgenis.auth.GroupAuthorityMetaData.GROUP_AUTHORITY;
import static org.molgenis.auth.GroupMetaData.GROUP;
import static org.molgenis.auth.UserAuthorityMetaData.USER_AUTHORITY;
import static org.molgenis.auth.UserMetaData.USER;
import static org.molgenis.data.i18n.model.I18nStringMetaData.I18N_STRING;
import static org.molgenis.data.i18n.model.LanguageMetadata.LANGUAGE;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;
import static org.molgenis.data.meta.model.TagMetadata.TAG;
import static org.molgenis.security.core.utils.SecurityUtils.*;

@Service
public class MolgenisSecurityWebAppDatabasePopulatorServiceImpl
		implements MolgenisSecurityWebAppDatabasePopulatorService
{
	private static final String USERNAME_ADMIN = "admin";
	private final UserFactory userFactory;
	private final GroupFactory groupFactory;
	private final UserAuthorityFactory userAuthorityFactory;
	private final GroupAuthorityFactory groupAuthorityFactory;

	@Value("${admin.password:@null}")
	private String adminPassword;
	@Value("${admin.email:molgenis+admin@gmail.com}")
	private String adminEmail;
	@Value("${anonymous.email:molgenis+anonymous@gmail.com}")
	private String anonymousEmail;

	@Autowired
	MolgenisSecurityWebAppDatabasePopulatorServiceImpl(UserFactory userFactory, GroupFactory groupFactory,
			UserAuthorityFactory userAuthorityFactory, GroupAuthorityFactory groupAuthorityFactory)
	{
		this.userFactory = requireNonNull(userFactory);
		this.groupFactory = requireNonNull(groupFactory);
		this.userAuthorityFactory = requireNonNull(userAuthorityFactory);
		this.groupAuthorityFactory = requireNonNull(groupAuthorityFactory);
	}

	@Override
	@Transactional
	@RunAsSystem
	public void populateDatabase(DataService dataService, String homeControllerId, String userAccountControllerId)
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

		// set anonymous role for anonymous user
		UserAuthority anonymousAuthority = userAuthorityFactory.create();
		anonymousAuthority.setUser(anonymousUser);
		anonymousAuthority.setRole(AUTHORITY_ANONYMOUS);

		UserAuthority anonymousHomeAuthority = userAuthorityFactory.create();
		anonymousHomeAuthority.setUser(anonymousUser);
		anonymousHomeAuthority.setRole(AUTHORITY_PLUGIN_READ_PREFIX + homeControllerId);

		// create all users group
		Group allUsersGroup = groupFactory.create();
		allUsersGroup.setName(AccountService.ALL_USER_GROUP);

		// allow all users to see the home plugin
		GroupAuthority usersGroupHomeAuthority = groupAuthorityFactory.create();
		usersGroupHomeAuthority.setGroup(allUsersGroup);
		usersGroupHomeAuthority.setRole(AUTHORITY_PLUGIN_READ_PREFIX + homeControllerId);

		// allow all users to update their profile
		GroupAuthority usersGroupUserAccountAuthority = groupAuthorityFactory.create();
		usersGroupUserAccountAuthority.setGroup(allUsersGroup);
		usersGroupUserAccountAuthority.setRole(AUTHORITY_PLUGIN_WRITE_PREFIX + userAccountControllerId);

		// allow all users to read meta data entities
		List<String> entityNames = asList(ENTITY_TYPE_META_DATA, ATTRIBUTE_META_DATA, PACKAGE, TAG, LANGUAGE, I18N_STRING);
		Stream<GroupAuthority> entityGroupAuthorities = entityNames.stream().map(entityName ->
		{
			GroupAuthority usersGroupAuthority = groupAuthorityFactory.create();
			usersGroupAuthority.setGroup(allUsersGroup);
			usersGroupAuthority.setRole(AUTHORITY_ENTITY_READ_PREFIX + entityName);
			return usersGroupAuthority;
		});

		// persist entities
		dataService.add(USER, Stream.of(userAdmin, anonymousUser));
		dataService.add(USER_AUTHORITY, Stream.of(anonymousAuthority, anonymousHomeAuthority));
		dataService.add(GROUP, allUsersGroup);
		dataService.add(GROUP_AUTHORITY,
				Stream.concat(Stream.of(usersGroupHomeAuthority, usersGroupUserAccountAuthority),
						entityGroupAuthorities));
	}

}