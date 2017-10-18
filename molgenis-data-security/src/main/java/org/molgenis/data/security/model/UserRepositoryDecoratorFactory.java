package org.molgenis.data.security.model;

import org.molgenis.data.AbstractSystemRepositoryDecoratorFactory;
import org.molgenis.data.Repository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

@Component
public class UserRepositoryDecoratorFactory extends AbstractSystemRepositoryDecoratorFactory<UserEntity, UserMetadata>
{
	private final PasswordEncoder passwordEncoder;

	public UserRepositoryDecoratorFactory(UserMetadata userMetaData, PasswordEncoder passwordEncoder)
	{
		super(userMetaData);
		this.passwordEncoder = requireNonNull(passwordEncoder);
	}

	@Override
	public Repository<UserEntity> createDecoratedRepository(Repository<UserEntity> repository)
	{
		return new UserRepositoryDecorator(repository, passwordEncoder);
	}
}
