package org.molgenis.data.security.auth;

import static java.util.Objects.requireNonNull;

import java.util.stream.Stream;
import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Repository;
import org.molgenis.data.UnknownEntityException;

class UserRepositoryValidationDecorator extends AbstractRepositoryDecorator<User> {

  private final UserValidator userValidator;

  UserRepositoryValidationDecorator(
      Repository<User> delegateRepository, UserValidator userValidator) {
    super(delegateRepository);
    this.userValidator = requireNonNull(userValidator);
  }

  @Override
  public void add(User user) {
    validateAddUser(user);
    super.add(user);
  }

  @Override
  public Integer add(Stream<User> userStream) {
    return super.add(userStream.filter(this::validateAddUser));
  }

  @Override
  public void update(User user) {
    validateUpdateUser(user);
    super.update(user);
  }

  @Override
  public void update(Stream<User> userStream) {
    super.update(userStream.filter(this::validateUpdateUser));
  }

  private boolean validateAddUser(User user) {
    userValidator.validate(user);
    return true;
  }

  private boolean validateUpdateUser(User user) {
    User existingUser = findOneById(user.getId());
    if (existingUser == null) {
      throw new UnknownEntityException(UserMetadata.USER, user.getId());
    }
    userValidator.validate(existingUser, user);
    return true;
  }
}
