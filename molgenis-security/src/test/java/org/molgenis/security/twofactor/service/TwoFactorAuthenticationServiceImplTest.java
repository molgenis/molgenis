package org.molgenis.security.twofactor.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.populate.IdGeneratorImpl;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.user.UserService;
import org.molgenis.security.twofactor.exceptions.TooManyLoginAttemptsException;
import org.molgenis.security.twofactor.model.UserSecret;
import org.molgenis.security.twofactor.model.UserSecretFactory;
import org.molgenis.security.twofactor.model.UserSecretMetadata;
import org.molgenis.settings.AppSettings;
import org.springframework.security.test.context.annotation.SecurityTestExecutionListeners;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@SecurityTestExecutionListeners
class TwoFactorAuthenticationServiceImplTest {
  private static final String USERNAME = "molgenisUser";
  private static final String ROLE_SU = "SU";

  @Mock private DataService dataService;
  @Mock private UserService userService;
  @Mock private UserSecretFactory userSecretFactory;
  private TwoFactorAuthenticationService twoFactorAuthenticationServiceImpl;

  @BeforeEach
  void setUpBeforeMethod() {
    twoFactorAuthenticationServiceImpl =
        new TwoFactorAuthenticationServiceImpl(
            new OtpServiceImpl(mock(AppSettings.class)),
            dataService,
            userService,
            new IdGeneratorImpl(),
            userSecretFactory);
  }

  @Test
  void generateSecretKeyTest() {
    String key = twoFactorAuthenticationServiceImpl.generateSecretKey();
    assertTrue(key.matches("^[a-z0-9]+$"));
  }

  @Test
  void generateWrongSecretKeyTest() {
    String key = twoFactorAuthenticationServiceImpl.generateSecretKey();
    assertTrue(!key.matches("^[A-Z0-9]+$"));
  }

  @Test
  @WithMockUser(value = USERNAME, roles = ROLE_SU)
  void setSecretKeyTest() {
    User molgenisUser = mock(User.class);
    UserSecret userSecret = mock(UserSecret.class);
    when(molgenisUser.getId()).thenReturn("1324");
    when(userService.getUser(USERNAME)).thenReturn(molgenisUser);
    Query<UserSecret> userSecretQuery = mock(Query.class, RETURNS_SELF);

    String secretKey = "secretKey";
    when(userSecretFactory.create()).thenReturn(userSecret);
    twoFactorAuthenticationServiceImpl.saveSecretForUser(secretKey);
  }

  @Test
  @WithMockUser(value = USERNAME, roles = ROLE_SU)
  void isConfiguredForUserTest() {
    User molgenisUser = mock(User.class);
    UserSecret userSecret = mock(UserSecret.class);
    when(molgenisUser.getId()).thenReturn("1324");
    when(userService.getUser(USERNAME)).thenReturn(molgenisUser);
    Query<UserSecret> userSecretQuery = mock(Query.class, RETURNS_SELF);
    when(dataService.query(UserSecretMetadata.USER_SECRET, UserSecret.class))
        .thenReturn(userSecretQuery);
    when(userSecretQuery.eq(UserSecretMetadata.USER_ID, molgenisUser.getId()).findOne())
        .thenReturn(userSecret);

    when(userSecret.getSecret()).thenReturn("secretKey");
    boolean isConfigured = twoFactorAuthenticationServiceImpl.isConfiguredForUser();
    assertEquals(isConfigured, true);
  }

  @Test
  @WithMockUser(value = USERNAME, roles = ROLE_SU)
  void testUserIsBlocked() {
    User molgenisUser = mock(User.class);
    UserSecret userSecret = mock(UserSecret.class);
    when(molgenisUser.getId()).thenReturn("1324");
    when(userService.getUser(USERNAME)).thenReturn(molgenisUser);
    Query<UserSecret> userSecretQuery = mock(Query.class, RETURNS_SELF);
    when(dataService.query(UserSecretMetadata.USER_SECRET, UserSecret.class))
        .thenReturn(userSecretQuery);
    when(userSecretQuery.eq(UserSecretMetadata.USER_ID, molgenisUser.getId()).findOne())
        .thenReturn(userSecret);

    when(userSecret.getLastFailedAuthentication()).thenReturn(Instant.now());
    when(userSecret.getFailedLoginAttempts()).thenReturn(3);
    assertThrows(
        TooManyLoginAttemptsException.class,
        () -> twoFactorAuthenticationServiceImpl.userIsBlocked());
  }

  @Test
  @WithMockUser(value = USERNAME, roles = ROLE_SU)
  void testDisableForUser() {
    User molgenisUser = mock(User.class);
    UserSecret userSecret = mock(UserSecret.class);
    when(molgenisUser.getUsername()).thenReturn(USERNAME);
    when(molgenisUser.getId()).thenReturn("1324");
    when(userService.getUser(USERNAME)).thenReturn(molgenisUser);
    Query<UserSecret> userSecretQuery = mock(Query.class, RETURNS_SELF);
    when(dataService.query(UserSecretMetadata.USER_SECRET, UserSecret.class))
        .thenReturn(userSecretQuery);
    when(userSecretQuery.eq(UserSecretMetadata.USER_ID, molgenisUser.getId()).findOne())
        .thenReturn(userSecret);

    when(userService.getUser(molgenisUser.getUsername())).thenReturn(molgenisUser);
    when(dataService
            .query(UserSecretMetadata.USER_SECRET, UserSecret.class)
            .eq(UserSecretMetadata.USER_ID, molgenisUser.getId())
            .findOne())
        .thenReturn(userSecret);
    twoFactorAuthenticationServiceImpl.disableForUser();
  }
}
