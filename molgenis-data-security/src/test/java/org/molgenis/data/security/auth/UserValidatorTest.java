package org.molgenis.data.security.auth;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.molgenis.test.AbstractMockitoTestNGSpringContextTests;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@ContextConfiguration(classes = {UserValidatorTest.Config.class})
@TestExecutionListeners(listeners = {WithSecurityContextTestExecutionListener.class})
public class UserValidatorTest extends AbstractMockitoTestNGSpringContextTests {
  private UserValidator userValidator;

  @BeforeMethod
  public void setUpBeforeMethod() {
    userValidator = new UserValidator();
  }

  @Test
  @WithMockUser
  public void testValidateCreateNonSuperuserAsNonSuperuser() {
    User user = mock(User.class);
    userValidator.validate(user);
    // test passes if no exception occurred
  }

  @Test
  @WithMockUser(roles = "SU")
  public void testValidateCreateNonSuperuserAsSuperuser() {
    User user = mock(User.class);
    userValidator.validate(user);
    // test passes if no exception occurred
  }

  @Test(expectedExceptions = UserSuModificationException.class)
  @WithMockUser
  public void testValidateCreateSuperuserAsNonSuperuser() {
    User user = when(mock(User.class).isSuperuser()).thenReturn(true).getMock();
    userValidator.validate(user);
  }

  @Test
  @WithMockUser(roles = "SU")
  public void testValidateCreateSuperuserAsSuperuser() {
    User user = when(mock(User.class).isSuperuser()).thenReturn(true).getMock();
    userValidator.validate(user);
    // test passes if no exception occurred
  }

  @Test
  @WithMockUser
  public void testValidateUpdateNonSuperuserAsNonSuperuser() {
    User user = mock(User.class);
    User updatedUser = mock(User.class);
    userValidator.validate(user, updatedUser);
    // test passes if no exception occurred
  }

  @Test
  @WithMockUser(roles = "SU")
  public void testValidateUpdateNonSuperuserAsSuperuser() {
    User user = mock(User.class);
    User updatedUser = mock(User.class);
    userValidator.validate(user, updatedUser);
    // test passes if no exception occurred
  }

  @Test(expectedExceptions = UserSuModificationException.class)
  @WithMockUser
  public void testValidateUpdateSuperuserAsNonSuperuser() {
    User user = when(mock(User.class).isSuperuser()).thenReturn(true).getMock();
    User updatedUser = mock(User.class);
    userValidator.validate(user, updatedUser);
  }

  @Test
  @WithMockUser(roles = "SU")
  public void testValidateUpdateSuperuserAsSuperuser() {
    User user = when(mock(User.class).isSuperuser()).thenReturn(true).getMock();
    User updatedUser = mock(User.class);
    userValidator.validate(user, updatedUser);
    // test passes if no exception occurred
  }

  @Test(expectedExceptions = UserSuModificationException.class)
  @WithMockUser
  public void testValidateEnableSuperuserAsNonSuperuser() {
    User user = mock(User.class);
    User updatedUser = when(mock(User.class).isSuperuser()).thenReturn(true).getMock();
    userValidator.validate(user, updatedUser);
  }

  @Test
  @WithMockUser(roles = "SU")
  public void testValidateEnableSuperuserAsSuperuser() {
    User user = mock(User.class);
    User updatedUser = when(mock(User.class).isSuperuser()).thenReturn(true).getMock();
    userValidator.validate(user, updatedUser);
    // test passes if no exception occurred
  }

  @Configuration
  static class Config {}
}
