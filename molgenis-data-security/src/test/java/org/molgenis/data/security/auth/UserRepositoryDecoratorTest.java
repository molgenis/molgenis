package org.molgenis.data.security.auth;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.*;
import static org.molgenis.data.security.auth.UserRepositoryDecorator.DELETE_USER_MSG;

import java.util.List;
import java.util.stream.Stream;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class UserRepositoryDecoratorTest extends AbstractMockitoTest {
  @Mock private Repository<User> delegateRepository;
  @Mock private PasswordEncoder passwordEncoder;

  private UserRepositoryDecorator userRepositoryDecorator;

  @BeforeMethod
  public void setUp() {
    userRepositoryDecorator = new UserRepositoryDecorator(delegateRepository, passwordEncoder);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testUserRepositoryDecorator() {
    new UserRepositoryDecorator(null, null);
  }

  @Test
  public void addEntity() {
    String password = "password";
    User user = mock(User.class);
    when(user.getPassword()).thenReturn(password);
    userRepositoryDecorator.add(user);
    verify(passwordEncoder).encode(password);
    verify(delegateRepository).add(user);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void addStream() {
    String password = "password";
    User user0 = mock(User.class);
    when(user0.getPassword()).thenReturn(password);
    User user1 = mock(User.class);
    when(user1.getPassword()).thenReturn(password);

    when(delegateRepository.add(any(Stream.class)))
        .thenAnswer(
            invocation -> {
              Stream<Entity> entities = (Stream<Entity>) invocation.getArguments()[0];
              List<Entity> entitiesList = entities.collect(toList());
              return entitiesList.size();
            });
    Assert.assertEquals(userRepositoryDecorator.add(Stream.of(user0, user1)), Integer.valueOf(2));
    verify(passwordEncoder, times(2)).encode(password);
  }

  @Test(
      expectedExceptions = UnsupportedOperationException.class,
      expectedExceptionsMessageRegExp = DELETE_USER_MSG)
  public void delete() {
    User user = mock(User.class);
    userRepositoryDecorator.delete(user);
  }

  @Test(
      expectedExceptions = UnsupportedOperationException.class,
      expectedExceptionsMessageRegExp = DELETE_USER_MSG)
  public void deleteStream() {
    User user = mock(User.class);
    Stream<User> entities = Stream.of(user);
    userRepositoryDecorator.delete(entities);
  }

  @Test(
      expectedExceptions = UnsupportedOperationException.class,
      expectedExceptionsMessageRegExp = DELETE_USER_MSG)
  public void deleteById() {
    User user = mock(User.class);
    userRepositoryDecorator.delete(user);
  }

  @Test(
      expectedExceptions = UnsupportedOperationException.class,
      expectedExceptionsMessageRegExp = DELETE_USER_MSG)
  public void deleteAllStream() {
    userRepositoryDecorator.deleteAll(Stream.of("1"));
  }

  @Test(
      expectedExceptions = UnsupportedOperationException.class,
      expectedExceptionsMessageRegExp = DELETE_USER_MSG)
  public void deleteAll() {
    userRepositoryDecorator.deleteAll();
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  public void updateStream() {
    when(passwordEncoder.encode("password")).thenReturn("passwordHash");

    User currentUser = mock(User.class);
    when(currentUser.getPassword()).thenReturn("currentPasswordHash");
    when(userRepositoryDecorator.findOneById("1")).thenReturn(currentUser);

    User user = mock(User.class);
    when(user.getId()).thenReturn("1");
    when(user.getPassword()).thenReturn("password");

    Stream<User> entities = Stream.of(user);
    ArgumentCaptor<Stream<User>> captor = ArgumentCaptor.forClass(Stream.class);
    doNothing().when(delegateRepository).update(captor.capture());
    userRepositoryDecorator.update(entities);
    Assert.assertEquals(captor.getValue().collect(toList()), singletonList(user));
    verify(user).setPassword("passwordHash");
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  public void updateStreamUnchangedPassword() {
    User currentUser = mock(User.class);
    when(currentUser.getPassword()).thenReturn("currentPasswordHash");
    when(userRepositoryDecorator.findOneById("1")).thenReturn(currentUser);

    User user = mock(User.class);
    when(user.getId()).thenReturn("1");
    when(user.getPassword()).thenReturn("currentPasswordHash");

    Stream<User> entities = Stream.of(user);
    ArgumentCaptor<Stream<User>> captor = ArgumentCaptor.forClass(Stream.class);
    doNothing().when(delegateRepository).update(captor.capture());
    userRepositoryDecorator.update(entities);
    Assert.assertEquals(captor.getValue().collect(toList()), singletonList(user));
    verify(user).setPassword("currentPasswordHash");
  }
}
