package org.molgenis.auth;

import org.molgenis.data.AbstractSystemRepositoryDecoratorFactory;
import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

@Component
public class UserRepositoryDecoratorFactory extends AbstractSystemRepositoryDecoratorFactory<User, UserMetaData>
{
	private final UserAuthorityFactory userAuthorityFactory;
	private final DataService dataService;
	private final PasswordEncoder passwordEncoder;

	public UserRepositoryDecoratorFactory(UserMetaData userMetaData, UserAuthorityFactory userAuthorityFactory,
			DataService dataService, PasswordEncoder passwordEncoder)
	{
		super(userMetaData);
		this.userAuthorityFactory = requireNonNull(userAuthorityFactory);
		this.dataService = requireNonNull(dataService);
		this.passwordEncoder = requireNonNull(passwordEncoder);
	}

	@Override
	public Repository<User> createDecoratedRepository(Repository<User> repository)
	{
		return new UserRepositoryDecorator(repository, userAuthorityFactory, dataService, passwordEncoder);
	}
}
