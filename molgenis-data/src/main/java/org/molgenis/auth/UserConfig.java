package org.molgenis.auth;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryDecoratorRegistry;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.PostConstruct;

import static java.util.Objects.requireNonNull;
import static org.molgenis.auth.UserMetaData.USER;

@Configuration
public class UserConfig
{
	private final RepositoryDecoratorRegistry repositoryDecoratorRegistry;
	private final UserAuthorityFactory userAuthorityFactory;
	private final DataService dataService;
	private final PasswordEncoder passwordEncoder;

	public UserConfig(RepositoryDecoratorRegistry repositoryDecoratorRegistry,
			UserAuthorityFactory userAuthorityFactory, DataService dataService, PasswordEncoder passwordEncoder)
	{
		this.repositoryDecoratorRegistry = requireNonNull(repositoryDecoratorRegistry);
		this.userAuthorityFactory = requireNonNull(userAuthorityFactory);
		this.dataService = requireNonNull(dataService);
		this.passwordEncoder = requireNonNull(passwordEncoder);
	}

	@SuppressWarnings("unchecked")
	@PostConstruct
	public void init()
	{
		repositoryDecoratorRegistry.addFactory(USER,
				repository -> (Repository<Entity>) (Repository<? extends Entity>) new UserRepositoryDecorator(
						(Repository<User>) (Repository<? extends Entity>) repository, userAuthorityFactory, dataService,
						passwordEncoder));
	}
}
