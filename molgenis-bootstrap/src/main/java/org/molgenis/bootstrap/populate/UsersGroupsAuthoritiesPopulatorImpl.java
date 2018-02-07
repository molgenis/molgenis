package org.molgenis.bootstrap.populate;

import org.molgenis.core.ui.admin.user.UserAccountController;
import org.molgenis.data.DataService;
import org.molgenis.data.security.auth.*;
import org.molgenis.security.account.AccountService;
import org.molgenis.security.core.runas.RunAsSystem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.decorator.meta.DecoratorConfigurationMetadata.DECORATOR_CONFIGURATION;
import static org.molgenis.data.file.model.FileMetaMetaData.FILE_META;
import static org.molgenis.data.i18n.model.L10nStringMetaData.L10N_STRING;
import static org.molgenis.data.i18n.model.LanguageMetadata.LANGUAGE;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.EntityTypeMetadata.ENTITY_TYPE_META_DATA;
import static org.molgenis.data.meta.model.PackageMetadata.PACKAGE;
import static org.molgenis.data.meta.model.TagMetadata.TAG;
import static org.molgenis.data.security.auth.GroupAuthorityMetaData.GROUP_AUTHORITY;
import static org.molgenis.data.security.auth.GroupMetaData.GROUP;
import static org.molgenis.data.security.auth.UserAuthorityMetaData.USER_AUTHORITY;
import static org.molgenis.data.security.auth.UserMetaData.USER;
import static org.molgenis.data.security.owned.OwnedEntityType.OWNED;
import static org.molgenis.security.core.utils.SecurityUtils.*;

@Service
public class UsersGroupsAuthoritiesPopulatorImpl implements UsersGroupsAuthoritiesPopulator
{
	private static final String USERNAME_ADMIN = "admin";

	private final DataService dataService;
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

	UsersGroupsAuthoritiesPopulatorImpl(DataService dataService, UserFactory userFactory, GroupFactory groupFactory,
			UserAuthorityFactory userAuthorityFactory, GroupAuthorityFactory groupAuthorityFactory)
	{
		this.dataService = requireNonNull(dataService);
		this.userFactory = requireNonNull(userFactory);
		this.groupFactory = requireNonNull(groupFactory);
		this.userAuthorityFactory = requireNonNull(userAuthorityFactory);
		this.groupAuthorityFactory = requireNonNull(groupAuthorityFactory);
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

		// set anonymous role for anonymous user
		UserAuthority anonymousAuthority = userAuthorityFactory.create();
		anonymousAuthority.setUser(anonymousUser);
		anonymousAuthority.setRole(AUTHORITY_ANONYMOUS);

		// create all users group
		Group allUsersGroup = groupFactory.create();
		allUsersGroup.setName(AccountService.ALL_USER_GROUP);

		// allow all users to update their profile
		GroupAuthority usersGroupUserAccountAuthority = groupAuthorityFactory.create();
		usersGroupUserAccountAuthority.setGroup(allUsersGroup);
		usersGroupUserAccountAuthority.setRole(AUTHORITY_PLUGIN_WRITE_PREFIX + UserAccountController.ID);

		// allow all users to read meta data entities
		List<String> entityTypeIds = asList(ENTITY_TYPE_META_DATA, ATTRIBUTE_META_DATA, PACKAGE, TAG, LANGUAGE,
				L10N_STRING, FILE_META, OWNED, DECORATOR_CONFIGURATION);
		Stream<GroupAuthority> entityGroupAuthorities = entityTypeIds.stream().map(entityTypeId ->
		{
			GroupAuthority usersGroupAuthority = groupAuthorityFactory.create();
			usersGroupAuthority.setGroup(allUsersGroup);
			usersGroupAuthority.setRole(AUTHORITY_ENTITY_READ_PREFIX + entityTypeId);
			return usersGroupAuthority;
		});

		// persist entities
		dataService.add(USER, Stream.of(userAdmin, anonymousUser));
		dataService.add(USER_AUTHORITY, anonymousAuthority);
		dataService.add(GROUP, allUsersGroup);
		dataService.add(GROUP_AUTHORITY,
				Stream.concat(Stream.of(usersGroupUserAccountAuthority), entityGroupAuthorities));
	}
}