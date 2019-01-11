package org.molgenis.data.security.auth;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.AbstractSystemRepositoryDecoratorFactory;
import org.molgenis.data.Repository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserRepositoryDecoratorFactory
    extends AbstractSystemRepositoryDecoratorFactory<User, UserMetadata> {
  private final PasswordEncoder passwordEncoder;
  private final UserValidator userValidator;

  public UserRepositoryDecoratorFactory(
      UserMetadata userMetadata, PasswordEncoder passwordEncoder, UserValidator userValidator) {
    super(userMetadata);
    this.passwordEncoder = requireNonNull(passwordEncoder);
    this.userValidator = requireNonNull(userValidator);
  }

  @Override
  public Repository<User> createDecoratedRepository(Repository<User> repository) {
    Repository<User> decoratedRepository = new UserRepositoryDecorator(repository, passwordEncoder);
    decoratedRepository = new UserRepositoryValidationDecorator(decoratedRepository, userValidator);
    return decoratedRepository;
  }
}
