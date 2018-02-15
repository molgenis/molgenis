package org.molgenis.data.security.auth;

import org.molgenis.data.AbstractSystemRepositoryDecoratorFactory;
import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

@Component
public class UserRepositoryDecoratorFactory extends AbstractSystemRepositoryDecoratorFactory<User, UserMetaData>
{
	private final DataService dataService;
	private final PasswordEncoder passwordEncoder;

	public UserRepositoryDecoratorFactory(UserMetaData userMetaData,
			DataService dataService, PasswordEncoder passwordEncoder)
	{
		super(userMetaData);
		this.dataService = requireNonNull(dataService);
		this.passwordEncoder = requireNonNull(passwordEncoder);
	}

	@Override
	public Repository<User> createDecoratedRepository(Repository<User> repository)
	{
		return new UserRepositoryDecorator(repository, dataService, passwordEncoder);
	}
}
