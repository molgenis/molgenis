package org.molgenis.data.security.auth;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.test.AbstractMockitoSpringContextTests;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;

@ContextConfiguration(classes = {UserValidatorTest.Config.class})
@TestExecutionListeners(listeners = {WithSecurityContextTestExecutionListener.class})
class UserValidatorTest extends AbstractMockitoSpringContextTests {
  private UserValidator userValidator;

  @BeforeEach
  void setUpBeforeMethod() {
    userValidator = new UserValidator();
  }

  @Test
  @WithMockUser
  void testValidateCreateNonSuperuserAsNonSuperuser() {
    User user = mock(User.class);
    userValidator.validate(user);
    // test passes if no exception occurred
  }

  @Test
  @WithMockUser(roles = "SU")
  void testValidateCreateNonSuperuserAsSuperuser() {
    User user = mock(User.class);
    userValidator.validate(user);
    // test passes if no exception occurred
  }

  @Test
  @WithMockUser
  void testValidateCreateSuperuserAsNonSuperuser() {
    User user = when(mock(User.class).isSuperuser()).thenReturn(true).getMock();
    assertThrows(UserSuModificationException.class, () -> userValidator.validate(user));
  }

  @Test
  @WithMockUser(roles = "SU")
  void testValidateCreateSuperuserAsSuperuser() {
    User user = when(mock(User.class).isSuperuser()).thenReturn(true).getMock();
    userValidator.validate(user);
    // test passes if no exception occurred
  }

  @Test
  @WithMockUser
  void testValidateUpdateNonSuperuserAsNonSuperuser() {
    User user = mock(User.class);
    User updatedUser = mock(User.class);
    userValidator.validate(user, updatedUser);
    // test passes if no exception occurred
  }

  @Test
  @WithMockUser(roles = "SU")
  void testValidateUpdateNonSuperuserAsSuperuser() {
    User user = mock(User.class);
    User updatedUser = mock(User.class);
    userValidator.validate(user, updatedUser);
    // test passes if no exception occurred
  }

  @Test
  @WithMockUser
  void testValidateUpdateSuperuserAsNonSuperuser() {
    User user = when(mock(User.class).isSuperuser()).thenReturn(true).getMock();
    User updatedUser = mock(User.class);
    assertThrows(
        UserSuModificationException.class, () -> userValidator.validate(user, updatedUser));
  }

  @Test
  @WithMockUser(roles = "SU")
  void testValidateUpdateSuperuserAsSuperuser() {
    User user = when(mock(User.class).isSuperuser()).thenReturn(true).getMock();
    User updatedUser = mock(User.class);
    userValidator.validate(user, updatedUser);
    // test passes if no exception occurred
  }

  @Test
  @WithMockUser
  void testValidateEnableSuperuserAsNonSuperuser() {
    User user = mock(User.class);
    User updatedUser = when(mock(User.class).isSuperuser()).thenReturn(true).getMock();
    assertThrows(
        UserSuModificationException.class, () -> userValidator.validate(user, updatedUser));
  }

  @Test
  @WithMockUser(roles = "SU")
  void testValidateEnableSuperuserAsSuperuser() {
    User user = mock(User.class);
    User updatedUser = when(mock(User.class).isSuperuser()).thenReturn(true).getMock();
    userValidator.validate(user, updatedUser);
    // test passes if no exception occurred
  }

  @Configuration
  static class Config {}
}
