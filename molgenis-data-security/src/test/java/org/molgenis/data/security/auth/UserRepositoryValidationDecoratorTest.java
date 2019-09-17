package org.molgenis.data.security.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.Repository;
import org.molgenis.test.AbstractMockitoTest;

class UserRepositoryValidationDecoratorTest extends AbstractMockitoTest {
  @Mock private Repository<User> delegateRepository;
  @Mock private UserValidator userValidator;
  private UserRepositoryValidationDecorator userRepositoryValidationDecorator;

  @BeforeEach
  void setUpBeforeMethod() {
    userRepositoryValidationDecorator =
        new UserRepositoryValidationDecorator(delegateRepository, userValidator);
  }

  @Test
  void testAdd() {
    User user = mock(User.class);
    userRepositoryValidationDecorator.add(user);
    verify(userValidator).validate(user);
  }

  @SuppressWarnings("unchecked")
  @Test
  void testAddStream() {
    User user = mock(User.class);

    when(delegateRepository.add(any(Stream.class)))
        .thenAnswer(
            invocation ->
                Long.valueOf(((Stream<User>) invocation.getArgument(0)).count()).intValue());

    userRepositoryValidationDecorator.add(Stream.of(user));
    verify(userValidator).validate(user);
  }

  @Test
  void testUpdate() {
    String userId = "MyUserId";
    User user = mock(User.class);
    when(user.getId()).thenReturn(userId);

    User existingUser = mock(User.class);
    when(delegateRepository.findOneById(userId)).thenReturn(existingUser);

    userRepositoryValidationDecorator.update(user);
    verify(userValidator).validate(existingUser, user);
  }

  @SuppressWarnings({"unchecked", "ResultOfMethodCallIgnored"})
  @Test
  void testUpdateStream() {
    String userId = "MyUserId";
    User user = mock(User.class);
    when(user.getId()).thenReturn(userId);

    User existingUser = mock(User.class);
    when(delegateRepository.findOneById(userId)).thenReturn(existingUser);

    doAnswer(
            invocation -> {
              ((Stream<User>) invocation.getArgument(0)).count();
              return null;
            })
        .when(delegateRepository)
        .update(any(Stream.class));

    userRepositoryValidationDecorator.update(Stream.of(user));
    verify(userValidator).validate(existingUser, user);
  }
}
