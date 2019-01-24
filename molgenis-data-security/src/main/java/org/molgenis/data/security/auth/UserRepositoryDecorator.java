package org.molgenis.data.security.auth;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.Iterators;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.molgenis.data.AbstractRepositoryDecorator;
import org.molgenis.data.Repository;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserRepositoryDecorator extends AbstractRepositoryDecorator<User> {
  static final String DELETE_USER_MSG = "Users can't be deleted. Disable them instead.";

  private static final int BATCH_SIZE = 1000;
  private final PasswordEncoder passwordEncoder;

  UserRepositoryDecorator(Repository<User> delegateRepository, PasswordEncoder passwordEncoder) {
    super(delegateRepository);
    this.passwordEncoder = requireNonNull(passwordEncoder);
  }

  @Override
  public void add(User entity) {
    encodePassword(entity);
    delegate().add(entity);
  }

  @Override
  public void update(User entity) {
    updatePassword(entity);
    delegate().update(entity);
  }

  @Override
  public Integer add(Stream<User> entities) {
    AtomicInteger count = new AtomicInteger();
    Iterators.partition(entities.iterator(), BATCH_SIZE)
        .forEachRemaining(
            users -> {
              users.forEach(this::encodePassword);

              Integer batchCount = delegate().add(users.stream());
              count.addAndGet(batchCount);
            });
    return count.get();
  }

  @Override
  public void update(Stream<User> entities) {
    entities =
        entities.filter(
            entity -> {
              updatePassword(entity);
              return true;
            });
    delegate().update(entities);
  }

  private void updatePassword(User user) {
    User currentUser = findOneById(user.getId());

    String currentPassword = currentUser.getPassword();
    String password = user.getPassword();
    // password is updated
    if (!currentPassword.equals(password)) {
      password = passwordEncoder.encode(user.getPassword());
    }
    user.setPassword(password);
  }

  private void encodePassword(User user) {
    String password = user.getPassword();
    String encodedPassword = passwordEncoder.encode(password);
    user.setPassword(encodedPassword);
  }

  @Override
  public void delete(User entity) {
    throw new UnsupportedOperationException(DELETE_USER_MSG);
  }

  @Override
  public void delete(Stream<User> entities) {
    throw new UnsupportedOperationException(DELETE_USER_MSG);
  }

  @Override
  public void deleteById(Object id) {
    throw new UnsupportedOperationException(DELETE_USER_MSG);
  }

  @Override
  public void deleteAll(Stream<Object> ids) {
    throw new UnsupportedOperationException(DELETE_USER_MSG);
  }

  @Override
  public void deleteAll() {
    throw new UnsupportedOperationException(DELETE_USER_MSG);
  }
}
