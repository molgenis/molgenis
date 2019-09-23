package org.molgenis.security.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.user.UserService;
import org.molgenis.test.AbstractMockitoSpringContextTests;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.annotation.SecurityTestExecutionListeners;
import org.springframework.security.test.context.support.WithMockUser;

@SecurityTestExecutionListeners
class UserAccountServiceImplTest extends AbstractMockitoSpringContextTests {
  private static final String USERNAME_USER = "username";
  private static Authentication authentication;

  @Mock private UserService userService;
  @Mock private PasswordEncoder passwordEncoder;
  private UserAccountServiceImpl userAccountServiceImpl;

  @BeforeEach
  void setUpBeforeEach() {
    this.userAccountServiceImpl = new UserAccountServiceImpl();
    this.userAccountServiceImpl.setUserService(userService);
    this.userAccountServiceImpl.setPasswordEncoder(passwordEncoder);
  }

  @Test
  @WithMockUser(USERNAME_USER)
  void getCurrentUser() {
    User existingUser = mock(User.class);
    when(userService.getUser(USERNAME_USER)).thenReturn(existingUser);
    assertEquals(existingUser, userAccountServiceImpl.getCurrentUser());
  }

  @Test
  @WithMockUser(USERNAME_USER)
  void updateCurrentUser() {
    User existingUser = mock(User.class);

    when(userService.getUser(USERNAME_USER)).thenReturn(existingUser);

    User updatedUser = mock(User.class);
    when(updatedUser.getUsername()).thenReturn("username");

    userAccountServiceImpl.updateCurrentUser(updatedUser);
    verify(passwordEncoder, never()).encode("encrypted-password");
  }

  @Test
  @WithMockUser(USERNAME_USER)
  void updateCurrentUser_wrongUser() {
    User updatedUser = mock(User.class);
    when(updatedUser.getUsername()).thenReturn("wrong-username");

    assertThrows(
        RuntimeException.class, () -> userAccountServiceImpl.updateCurrentUser(updatedUser));
  }

  @Test
  @WithMockUser(USERNAME_USER)
  void updateCurrentUser_changePassword() {
    User existingUser = mock(User.class);

    when(userService.getUser(USERNAME_USER)).thenReturn(existingUser);

    User updatedUser = mock(User.class);
    when(updatedUser.getUsername()).thenReturn("username");

    userAccountServiceImpl.updateCurrentUser(updatedUser);
  }

  @Test
  @WithMockUser(USERNAME_USER)
  void validateCurrentUserPassword() {
    User existingUser = mock(User.class);
    when(existingUser.getPassword()).thenReturn("encrypted-password");
    when(passwordEncoder.matches("password", "encrypted-password")).thenReturn(true);
    when(userService.getUser(USERNAME_USER)).thenReturn(existingUser);
    assertTrue(userAccountServiceImpl.validateCurrentUserPassword("password"));
    assertFalse(userAccountServiceImpl.validateCurrentUserPassword("wrong-password"));
  }
}
