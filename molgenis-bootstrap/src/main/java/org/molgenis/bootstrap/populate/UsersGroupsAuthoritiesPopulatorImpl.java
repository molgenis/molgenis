package org.molgenis.bootstrap.populate;

import org.molgenis.data.DataService;
import org.molgenis.data.security.model.UserEntity;
import org.molgenis.data.security.model.UserFactory;
import org.molgenis.security.core.runas.RunAsSystem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.security.model.UserMetadata.USER;
import static org.molgenis.security.core.utils.SecurityUtils.ANONYMOUS_USERNAME;

@Service
public class UsersGroupsAuthoritiesPopulatorImpl implements UsersGroupsAuthoritiesPopulator
{
	private static final String USERNAME_ADMIN = "admin";

	private final DataService dataService;
	private final UserFactory userFactory;

	@Value("${admin.password:@null}")
	private String adminPassword;
	@Value("${admin.email:molgenis+admin@gmail.com}")
	private String adminEmail;
	@Value("${anonymous.email:molgenis+anonymous@gmail.com}")
	private String anonymousEmail;

	UsersGroupsAuthoritiesPopulatorImpl(DataService dataService, UserFactory userFactory)
	{
		this.dataService = requireNonNull(dataService);
		this.userFactory = requireNonNull(userFactory);
	}

	@Override
	@Transactional
	@RunAsSystem
	public void populate()
	{
		if (adminPassword == null)
		{
			throw new IllegalStateException(
					"please configure the admin.password property in your molgenis-server.properties");
		}

		// create admin user
		UserEntity userAdmin = userFactory.create();
		userAdmin.setUsername(USERNAME_ADMIN);
		userAdmin.setPassword(adminPassword);
		userAdmin.setEmail(adminEmail);
		userAdmin.setActive(true);
		userAdmin.setSuperuser(true);
		userAdmin.setChangePassword(false);

		//FIXME: do we still need this?
		// create anonymous user
		UserEntity anonymousUser = userFactory.create();
		anonymousUser.setUsername(ANONYMOUS_USERNAME);
		anonymousUser.setPassword(ANONYMOUS_USERNAME);
		anonymousUser.setEmail(anonymousEmail);
		anonymousUser.setActive(true);
		anonymousUser.setSuperuser(false);
		anonymousUser.setChangePassword(false);

		// persist entities
		dataService.add(USER, Stream.of(userAdmin, anonymousUser));
	}
}