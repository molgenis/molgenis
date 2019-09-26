package org.molgenis.data.security.auth;

import static java.lang.Integer.valueOf;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.security.auth.UserRepositoryDecorator.DELETE_USER_MSG;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.crypto.password.PasswordEncoder;

class UserRepositoryDecoratorTest extends AbstractMockitoTest {
  @Mock private Repository<User> delegateRepository;
  @Mock private PasswordEncoder passwordEncoder;

  private UserRepositoryDecorator userRepositoryDecorator;

  @BeforeEach
  void setUp() {
    userRepositoryDecorator = new UserRepositoryDecorator(delegateRepository, passwordEncoder);
  }

  @Test
  void testUserRepositoryDecorator() {
    assertThrows(NullPointerException.class, () -> new UserRepositoryDecorator(null, null));
  }

  @Test
  void addEntity() {
    String password = "password";
    User user = mock(User.class);
    when(user.getPassword()).thenReturn(password);
    userRepositoryDecorator.add(user);
    verify(passwordEncoder).encode(password);
    verify(delegateRepository).add(user);
  }

  @SuppressWarnings("unchecked")
  @Test
  void addStream() {
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
    assertEquals(valueOf(2), userRepositoryDecorator.add(of(user0, user1)));
    verify(passwordEncoder, times(2)).encode(password);
  }

  @Test
  void delete() {
    User user = mock(User.class);
    Exception exception =
        assertThrows(
            UnsupportedOperationException.class, () -> userRepositoryDecorator.delete(user));
    assertThat(exception.getMessage()).containsPattern(DELETE_USER_MSG);
  }

  @Test
  void deleteStream() {
    User user = mock(User.class);
    Stream<User> entities = Stream.of(user);
    Exception exception =
        assertThrows(
            UnsupportedOperationException.class, () -> userRepositoryDecorator.delete(entities));
    assertThat(exception.getMessage()).containsPattern(DELETE_USER_MSG);
  }

  @Test
  void deleteById() {
    User user = mock(User.class);
    Exception exception =
        assertThrows(
            UnsupportedOperationException.class, () -> userRepositoryDecorator.delete(user));
    assertThat(exception.getMessage()).containsPattern(DELETE_USER_MSG);
  }

  @Test
  void deleteAllStream() {
    Exception exception =
        assertThrows(
            UnsupportedOperationException.class,
            () -> userRepositoryDecorator.deleteAll(Stream.of("1")));
    assertThat(exception.getMessage()).containsPattern(DELETE_USER_MSG);
  }

  @Test
  void deleteAll() {
    Exception exception =
        assertThrows(
            UnsupportedOperationException.class, () -> userRepositoryDecorator.deleteAll());
    assertThat(exception.getMessage()).containsPattern(DELETE_USER_MSG);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  void updateStream() {
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
    assertEquals(singletonList(user), captor.getValue().collect(toList()));
    verify(user).setPassword("passwordHash");
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  void updateStreamUnchangedPassword() {
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
    assertEquals(singletonList(user), captor.getValue().collect(toList()));
    verify(user).setPassword("currentPasswordHash");
  }
}
