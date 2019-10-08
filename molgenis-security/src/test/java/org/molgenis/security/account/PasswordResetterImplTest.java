package org.molgenis.security.account;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.user.InvalidEmailAddressException;
import org.molgenis.data.security.user.UnknownUserException;
import org.molgenis.data.security.user.UserService;
import org.molgenis.settings.AppSettings;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class PasswordResetterImplTest extends AbstractMockitoTest {
  @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
  @Mock private UserService userService;
  @Mock private MailSender mailSender;
  @Mock private AppSettings appSettings;
  private PasswordResetterImpl passwordResetServiceImpl;

  @BeforeEach
  void setUpBeforeMethod() {
    passwordResetServiceImpl =
        new PasswordResetterImpl(
            passwordResetTokenRepository, userService, mailSender, appSettings);
  }

  @Test
  void testPasswordResetServiceImpl() {
    assertThrows(
        NullPointerException.class, () -> new PasswordResetterImpl(null, null, null, null));
  }

  @Test
  void testResetPassword() {
    String emailAddress = "my@email.com";

    User user = mock(User.class);
    when(user.getEmail()).thenReturn(emailAddress);

    when(userService.getUserByEmail(emailAddress)).thenReturn(user);

    String token = "MyToken";
    when(passwordResetTokenRepository.createToken(user)).thenReturn(token);

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setScheme("http");
    request.setServerName("my.host.org");
    request.setServerPort(80);
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

    String appTitle = "MyAppTitle";
    when(appSettings.getTitle()).thenReturn(appTitle);

    passwordResetServiceImpl.resetPassword(emailAddress);

    SimpleMailMessage simpleMessage = new SimpleMailMessage();
    simpleMessage.setTo(emailAddress);
    simpleMessage.setSubject("Password reset on MyAppTitle");
    simpleMessage.setText(
        "Hello,\n"
            + "\n"
            + "You are receiving this email because we received a password reset request for your account.\n"
            + "\n"
            + "http://my.host.org/account/password/change?username&token=MyToken\n"
            + "\n"
            + "If you did not request a password reset, you can safely ignore this email.");
    verify(mailSender).send(simpleMessage);
  }

  @Test
  void testResetPasswordUnknownEmailAddress() {
    String emailAddress = "unkown@email.com";
    assertThrows(
        InvalidEmailAddressException.class,
        () -> passwordResetServiceImpl.resetPassword(emailAddress));
  }

  @Test
  void testValidatePasswordResetToken() {
    String username = "MyUsername";
    String token = "MyToken";

    User user = mock(User.class);
    when(userService.getUser(username)).thenReturn(user);
    passwordResetServiceImpl.validatePasswordResetToken(username, token);
    verify(passwordResetTokenRepository).validateToken(user, token);
  }

  @Test
  void testValidatePasswordResetTokenUnknownUser() {
    String username = "MyUsername";
    String token = "MyToken";
    assertThrows(
        UnknownUserException.class,
        () -> passwordResetServiceImpl.validatePasswordResetToken(username, token));
  }

  @Test
  void testChangePassword() {
    String username = "MyUsername";
    String token = "MyToken";
    String password = "MyPassword";

    User user = mock(User.class);
    when(userService.getUser(username)).thenReturn(user);

    passwordResetServiceImpl.changePassword(username, token, password);
    verify(passwordResetTokenRepository).validateToken(user, token);
    verify(user).setPassword(password);
    verify(userService).update(user);
    verify(passwordResetTokenRepository).deleteToken(user, token);
  }

  @Test
  void testChangePasswordUnknownUser() {
    String username = "MyUsername";
    String token = "MyToken";
    String password = "MyPassword";

    assertThrows(
        UnknownUserException.class,
        () -> passwordResetServiceImpl.changePassword(username, token, password));
  }

  @Test
  void testChangePasswordAuthenticatedUser() {
    String username = "MyUsername";
    String password = "MyPassword";

    SecurityContext securityContext = mock(SecurityContext.class);
    Authentication authentication =
        new UsernamePasswordAuthenticationToken(username, "MyCurrentPassword");
    when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);

    User user = mock(User.class);
    when(userService.getUser(username)).thenReturn(user);
    passwordResetServiceImpl.changePasswordAuthenticatedUser(password);
    verify(user).setChangePassword(false);
    verify(userService).update(user);
  }

  @Test
  void testChangePasswordAuthenticatedUserNoAuthenticedUser() {
    SecurityContext securityContext = mock(SecurityContext.class);
    SecurityContextHolder.setContext(securityContext);
    assertThrows(
        AuthenticationCredentialsNotFoundException.class,
        () -> passwordResetServiceImpl.changePasswordAuthenticatedUser("MyPassword"));
  }
}
