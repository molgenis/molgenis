package org.molgenis.data.security.auth;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.AbstractSystemRepositoryDecoratorFactory;
import org.molgenis.data.Repository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserRepositoryDecoratorFactory
    extends AbstractSystemRepositoryDecoratorFactory<User, UserMetaData> {
  private final PasswordEncoder passwordEncoder;

  public UserRepositoryDecoratorFactory(
      UserMetaData userMetaData, PasswordEncoder passwordEncoder) {
    super(userMetaData);
    this.passwordEncoder = requireNonNull(passwordEncoder);
  }

  @Override
  public Repository<User> createDecoratedRepository(Repository<User> repository) {
    return new UserRepositoryDecorator(repository, passwordEncoder);
  }
}
